# 01 — Rol, Stack y Estructura de Paquetes

> [← Índice](../../CLAUDE.md)

---

## Rol y Contexto

Eres un asistente de desarrollo para un **Senior Backend Developer** experto en arquitectura de software.
Este proyecto aplica **Arquitectura Hexagonal**, **Clean Architecture**, **Clean Code**, **CQRS** y **Domain-Driven Design (DDD)**.
Asume siempre el máximo nivel de conocimiento técnico. No expliques conceptos básicos salvo que se te pida explícitamente.

---

## Stack Tecnológico

| Componente        | Versión   |
|-------------------|-----------|
| Java              | 25        |
| Spring Boot       | 4.0.3     |
| Gradle            | 9.4.0     |
| Gradle DSL        | Kotlin    |
| Gestión deps      | Version Catalogs (`gradle/libs.versions.toml`) |
| Migraciones BD    | Flyway    |
| Observabilidad    | Micrometer + OpenTelemetry |
| Calidad de código | Spotless + Checkstyle |
| Seguridad         | Spring Security 7     |

### Características de Java 25 a usar activamente

- **Records** para Value Objects, Commands, Queries y DTOs de respuesta.
- **Sealed classes + pattern matching** para modelar variantes del dominio (tipos algebraicos).
- **`switch` expressions con pattern matching** en lugar de cadenas if/else o instanceof.
- **Text blocks** para queries JPQL/SQL en tests, mensajes de error estructurados o JSON fixtures.
- **Virtual Threads (Project Loom)** — usar en lugar de `CompletableFuture` cuando la lógica es secuencial.
- **Sequenced Collections** donde aplique (`SequencedCollection`, `SequencedMap`).
- `var` en variables locales de tipo obvio; no usar en tipos genéricos complejos o lambdas.

---

## Estructura de Paquetes

Organización **feature-first**: el paquete raíz de cada funcionalidad de negocio contiene su propia arquitectura hexagonal completa. Esto garantiza el aislamiento entre contextos de dominio y evita que las capas horizontales se conviertan en cajones de sastre.

```
src/
├── main/java/com/{ctx}/
│   │
│   ├── shared/                             ← Shared Kernel: solo lo genuinamente compartido
│   │   ├── domain/
│   │   │   ├── exception/                  ← Jerarquía base de excepciones (DomainException…)
│   │   │   └── model/                      ← Value Objects transversales (Money, AuditTrail…)
│   │   └── infrastructure/
│   │       ├── config/                     ← Beans transversales (Security, OpenAPI, Flyway…)
│   │       └── persistence/                ← Configuración JPA base, AuditorAware
│   │
│   ├── order/                              ← Feature: Order (Bounded Context)
│   │   ├── domain/
│   │   │   ├── model/                      ← Order, OrderLine, OrderId, OrderStatus…
│   │   │   ├── service/                    ← PricingService, ShippingCostCalculator…
│   │   │   ├── event/                      ← OrderPlaced, OrderShipped, OrderCancelled…
│   │   │   └── exception/                  ← OrderNotFoundException, InsufficientStockException…
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   ├── input/
│   │   │   │   │   ├── command/            ← PlaceOrderUseCase, ShipOrderUseCase…
│   │   │   │   │   └── query/              ← GetOrderByIdUseCase, ListOrdersUseCase…
│   │   │   │   └── output/                 ← OrderRepositoryPort, OrderQueryPort…
│   │   │   ├── command/                    ← PlaceOrderHandler, ShipOrderHandler…
│   │   │   ├── query/                      ← GetOrderByIdHandler, ListOrdersHandler…
│   │   │   └── dto/                        ← PlaceOrderCommand, OrderDetailView…
│   │   └── infrastructure/
│   │       ├── adapter/
│   │       │   ├── input/
│   │       │   │   ├── rest/               ← OrderController, PlaceOrderRequest…
│   │       │   │   └── messaging/          ← OrderEventConsumer
│   │       │   └── output/
│   │       │       ├── persistence/        ← OrderJpaAdapter, OrderEntity, SpringOrderRepository…
│   │       │       └── messaging/          ← OrderEventProducer
│   │       └── config/                     ← OrderConfig (registro de beans de la feature)
│   │
│   ├── product/                            ← Feature: Product (Bounded Context)
│   │   ├── domain/
│   │   ├── application/
│   │   └── infrastructure/
│   │
│   └── customer/                           ← Feature: Customer (Bounded Context)
│       ├── domain/
│       ├── application/
│       └── infrastructure/
│
└── main/resources/
    ├── application.yml
    └── db/migration/                       ← Migraciones Flyway (todas en un solo lugar)
```

**Regla absoluta de dependencias dentro de cada feature:**
```
{feature}/infrastructure → {feature}/application → {feature}/domain
                                                          ↑
                                                    (núcleo puro)
```

### Reglas de comunicación entre features

Las features son **independientes entre sí**. Cuando una feature necesita datos de otra:

- Se referencia **solo por ID** (Value Object), nunca importando el modelo de dominio de otra feature.
- La coordinación entre features se hace mediante **Domain Events** — una feature publica, la otra escucha.
- Si una feature necesita mostrar datos de otra (p.ej. `Order` muestra el nombre del `Customer`), lo resuelve en el **Query Handler** de su propia feature llamando al Output Port correspondiente, nunca importando el Application Service de la otra.

```java
// ✅ order/domain/model/Order.java — referencia a Customer solo por ID
public class Order {
    private final CustomerId customerId;   // no importa nada de customer/
}

// ✅ order/application/port/output/CustomerDataPort.java
// Puerto de salida propio de la feature Order para obtener datos de Customer
public interface CustomerDataPort {
    Optional<CustomerSnapshot> findById(CustomerId id);
}

// CustomerSnapshot es un DTO propio de la feature Order — no el Customer de customer/
public record CustomerSnapshot(CustomerId id, String fullName, String email) {}
```

```java
// ✅ order/infrastructure/config/OrderConfig.java
@Bean
public CustomerDataPort customerDataPort(SpringCustomerRepository repository) {
    return customerId -> repository.findById(customerId.value())
            .map(e -> new CustomerSnapshot(CustomerId.of(e.getId()), e.getFullName(), e.getEmail()));
}
```

### Shared Kernel: qué puede y qué no puede entrar

| ✅ Entra en `shared/`                          | ❌ No entra en `shared/`                        |
|------------------------------------------------|------------------------------------------------|
| `Money`, `Currency`, `AuditTrail`              | Entidades de negocio (`Order`, `Product`…)     |
| Jerarquía base de excepciones                  | Application Services de ninguna feature        |
| Configuración técnica transversal              | Lógica de negocio de cualquier tipo            |
| `PageResponse<T>`, `IdempotencyPort` (base)    | DTOs específicos de una feature                |

**Si algo crece dentro de `shared/`, es una señal de que debería ser su propia feature.**

---

## Gestión de Dependencias: Version Catalogs

Todas las versiones se declaran en `gradle/libs.versions.toml`. **Nunca escribir versiones hardcodeadas en ningún `build.gradle.kts`.**

```toml
[versions]
java                 = "25"
spring-boot          = "4.0.3"
spring-dependency    = "1.1.x"
flyway               = "10.x"
mapstruct            = "1.6.x"
lombok               = "1.18.x"
archunit             = "1.3.x"
testcontainers       = "1.20.x"
micrometer-tracing   = "1.x"
spotless             = "6.x"
checkstyle           = "10.x"
logstash-encoder     = "7.x"

[libraries]
spring-boot-web          = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-jpa          = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-validation   = { module = "org.springframework.boot:spring-boot-starter-validation" }
spring-boot-actuator     = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-boot-test         = { module = "org.springframework.boot:spring-boot-starter-test" }
flyway-core              = { module = "org.flywaydb:flyway-core",                       version.ref = "flyway" }
flyway-postgres          = { module = "org.flywaydb:flyway-database-postgresql",        version.ref = "flyway" }
lombok                   = { module = "org.projectlombok:lombok",                       version.ref = "lombok" }
mapstruct                = { module = "org.mapstruct:mapstruct",                        version.ref = "mapstruct" }
mapstruct-processor      = { module = "org.mapstruct:mapstruct-processor",              version.ref = "mapstruct" }
archunit                 = { module = "com.tngtech.archunit:archunit-junit5",           version.ref = "archunit" }
testcontainers-junit     = { module = "org.testcontainers:junit-jupiter",               version.ref = "testcontainers" }
testcontainers-pg        = { module = "org.testcontainers:postgresql",                  version.ref = "testcontainers" }
micrometer-otlp          = { module = "io.micrometer:micrometer-registry-otlp",        version.ref = "micrometer-tracing" }
micrometer-tracing-otel  = { module = "io.micrometer:micrometer-tracing-bridge-otel",  version.ref = "micrometer-tracing" }
logstash-logback-encoder = { module = "net.logstash.logback:logstash-logback-encoder", version.ref = "logstash-encoder" }

[bundles]
spring-web      = ["spring-boot-web", "spring-boot-validation", "spring-boot-actuator"]
observability   = ["micrometer-otlp", "micrometer-tracing-otel"]
testing         = ["spring-boot-test", "archunit"]
testcontainers  = ["testcontainers-junit", "testcontainers-pg"]

[plugins]
spring-boot       = { id = "org.springframework.boot",        version.ref = "spring-boot" }
spring-dependency = { id = "io.spring.dependency-management", version.ref = "spring-dependency" }
spotless          = { id = "com.diffplug.spotless",           version.ref = "spotless" }
```

