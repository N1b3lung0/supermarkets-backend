# CLAUDE.md

## Índice

| # | Sección |
|---|---------|
| 1 | [Rol y Contexto](#rol-y-contexto) |
| 2 | [Stack Tecnológico](#stack-tecnológico) |
| 3 | [Estructura de Paquetes](#estructura-de-paquetes) |
| 4 | [Gestión de Dependencias: Version Catalogs](#gestión-de-dependencias-version-catalogs) |
| 5 | [DDD: Lenguaje Ubicuo](#ddd-lenguaje-ubicuo) |
| 6 | [DDD: Entidades, Aggregates y Value Objects](#ddd-entidades-aggregates-y-value-objects) |
| 7 | [DDD: Factory Methods en Aggregates](#ddd-factory-methods-en-aggregates) |
| 8 | [DDD: Domain Events](#ddd-domain-events) |
| 9 | [CQRS: Separación Command / Query](#cqrs-separación-command--query) |
| 10 | [Gestión de Transacciones](#gestión-de-transacciones) |
| 11 | [Jerarquía de Excepciones](#jerarquía-de-excepciones) |
| 12 | [Migraciones de Base de Datos: Flyway](#migraciones-de-base-de-datos-flyway) |
| 13 | [Convenciones de API REST](#convenciones-de-api-rest) |
| 14 | [Observabilidad](#observabilidad) |
| 15 | [Calidad de Código: Tooling](#calidad-de-código-tooling) |
| 16 | [Secrets en CI y entorno local](#secrets-en-ci-y-entorno-local) |
| 17 | [Desacoplamiento de Spring: Registro de Beans](#desacoplamiento-de-spring-registro-de-beans) |
| 18 | [Nomenclatura](#nomenclatura) |
| 19 | [Testing](#testing) |
| 20 | [ObjectMother: fixtures de test](#objectmother-fixtures-de-test) |
| 21 | [Conventional Commits](#conventional-commits-formato-de-mensajes) |
| 22 | [Pre-commit Hooks](#pre-commit-hooks) |
| 23 | [Git: Branch Strategy](#git-branch-strategy) |
| 24 | [Git: Branch Protection y PR Template](#git-branch-protection-y-pr-template) |
| 25 | [Seguridad: Scanning en CI](#seguridad-scanning-en-ci) |
| 26 | [Versionado Semántico y Release](#versionado-semántico-y-release) |
| 27 | [Dockerfile: convenciones](#dockerfile-convenciones) |
| 28 | [Estrategia de Despliegue](#estrategia-de-despliegue) |
| 29 | [Seguridad: Spring Security sin contaminar el dominio](#seguridad-spring-security-sin-contaminar-el-dominio) |
| 30 | [Diseño de Aggregates: Reglas de Oro](#diseño-de-aggregates-reglas-de-oro) |
| 31 | [Idempotencia](#idempotencia) |
| 32 | [Configuración Externalizada (12-Factor)](#configuración-externalizada-12-factor) |
| 33 | [OpenAPI / Swagger](#openapi--swagger-convenciones-de-documentación) |
| 34 | [Pipeline CI](#pipeline-ci-checks-obligatorios-antes-de-merge) |
| 35 | [Dependabot](#dependabot-actualizacion-automatica-de-dependencias) |
| 36 | [N+1 y Fetch Strategy en JPA](#n1-y-fetch-strategy-en-jpa) |
| 37 | [Auditoría sin contaminar el dominio](#auditoría-createdat-updatedat-createdby-sin-contaminar-el-dominio) |
| 38 | [Optimistic Locking](#optimistic-locking-concurrencia-sin-bloqueos) |
| 39 | [Soft Delete](#soft-delete-convención-única) |
| 40 | [Lo que NUNCA se debe hacer](#lo-que-nunca-se-debe-hacer) |
| 41 | [Respuestas de Claude](#respuestas-de-claude-cómo-trabajar-conmigo) |

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
// La implementación de CustomerDataPort puede delegar al repositorio de customer/
// pero eso es un detalle de infraestructura, invisible para el dominio de order/
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

---

## DDD: Lenguaje Ubicuo

El Lenguaje Ubicuo es **la regla más importante** del proyecto. Todo nombre en el código debe provenir del glosario del dominio acordado con el negocio, no de la tecnología.

### Reglas

- **Glosario primero.** Antes de crear una clase de dominio, el término debe estar definido y acordado. Si no tiene nombre en el negocio, no tiene clase en el dominio.
- **Un concepto, un nombre.** No mezclar sinónimos: si el negocio dice `Order`, el código dice `Order` — no `Purchase`, no `Cart`, no `Transaction`.
- **El dominio manda.** Si el negocio cambia el nombre de un concepto, se refactoriza el código.
- **Sin jerga técnica en el dominio.** `OrderManager`, `OrderProcessor`, `OrderHelper`, `OrderUtil` no son nombres de dominio — son síntomas de lógica de negocio mal ubicada.
- **Los métodos hablan el idioma del negocio.** `order.ship()`, `order.cancel(reason)`, `order.applyDiscount(coupon)` — no `order.setStatus(SHIPPED)` ni `order.updateData(dto)`.

```java
// ❌ Jerga técnica infiltrada en el dominio
public class OrderProcessor {
    public void processOrderData(OrderDTO dto) { ... }
}

// ✅ Lenguaje del negocio
public class Order {
    public void ship(TrackingNumber trackingNumber) { ... }
    public void cancel(CancellationReason reason) { ... }
}
```

---

## DDD: Entidades, Aggregates y Value Objects

### Entidades y Aggregates

- Las entidades tienen **identidad** modelada como Value Object, nunca `UUID` o `Long` en crudo.
- Los Aggregates protegen sus invariantes — toda modificación de estado pasa por métodos del Aggregate Root.
- Solo el Aggregate Root es persistido/recuperado por el repositorio. Nunca persistir entidades hijas directamente.
- No exponer colecciones internas mutables: devolver `List.copyOf()` o `Collections.unmodifiableList()`.

### Value Objects

Siempre inmutables. Usar `record`. Igualdad basada en valor. Validación en constructor.

```java
// shared/domain/model/Money.java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount required");
        Objects.requireNonNull(currency, "currency required");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Money amount cannot be negative: " + amount);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException(
                "Currency mismatch: cannot add %s and %s".formatted(this.currency, other.currency));
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

---

## DDD: Factory Methods en Aggregates

### Regla: constructor privado para reconstitución, `create()` estático para negocio

El Aggregate tiene dos formas de instanciarse con propósitos completamente distintos:

- **`create()` estático** — punto de entrada de negocio. Aplica invariantes, genera el ID, emite el Domain Event inicial. Es el único camino para crear un Aggregate nuevo.
- **Constructor de paquete/privado** — exclusivo para el mapper de persistencia. Reconstituye el Aggregate desde la base de datos sin disparar lógica de negocio ni eventos.

```java
// order/domain/model/Order.java
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    // ✅ Factory Method — único punto de entrada de negocio
    public static Order create(CustomerId customerId, List<OrderLineCommand> lineCommands) {
        Objects.requireNonNull(customerId, "customerId required");
        if (lineCommands == null || lineCommands.isEmpty())
            throw new IllegalArgumentException("Order must have at least one line");

        var order = new Order(OrderId.generate(), customerId, OrderStatus.PENDING, new ArrayList<>());
        lineCommands.forEach(cmd -> order.addLine(cmd.productId(), cmd.quantity(), cmd.unitPrice()));
        order.domainEvents.add(new OrderPlaced(order.id, customerId, Instant.now()));
        return order;
    }

    // ✅ Constructor de reconstitución — solo para el mapper de persistencia
    Order(OrderId id, CustomerId customerId, OrderStatus status, List<OrderLine> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.lines = new ArrayList<>(lines);
    }
}
```

```java
// order/infrastructure/adapter/output/persistence/mapper/OrderPersistenceMapper.java
@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderEntity entity) {
        // ✅ Usa el constructor de reconstitución — sin eventos, sin validaciones de creación
        return new Order(
            OrderId.of(entity.getId()),
            CustomerId.of(entity.getCustomerId()),
            OrderStatus.valueOf(entity.getStatus()),
            entity.getLines().stream().map(this::toLinesDomain).toList()
        );
    }
}
```

### Cuándo usar un `Builder` en lugar de Factory Method

Solo si el Aggregate tiene **más de 4-5 parámetros opcionales** en su creación. En ese caso, el Builder también es estático y anidado en la clase del Aggregate, nunca en una clase separada.

```java
// ✅ Builder anidado para Aggregates con muchos parámetros opcionales
var order = Order.builder()
    .customerId(customerId)
    .shippingAddress(address)
    .coupon(coupon)         // opcional
    .notes(notes)           // opcional
    .build();               // build() aplica invariantes y emite evento
```

---

## DDD: Domain Events

### Modelado

Los Domain Events son hechos del pasado. Siempre inmutables, siempre con timestamp.

```java
// order/domain/event/OrderEvent.java
public sealed interface OrderEvent permits OrderPlaced, OrderConfirmed, OrderShipped, OrderCancelled {}

public record OrderPlaced(OrderId orderId, CustomerId customerId, Instant occurredOn) implements OrderEvent {}
public record OrderConfirmed(OrderId orderId, Instant occurredOn) implements OrderEvent {}
public record OrderShipped(OrderId orderId, TrackingNumber trackingNumber, Instant occurredOn) implements OrderEvent {}
public record OrderCancelled(OrderId orderId, CancellationReason reason, Instant occurredOn) implements OrderEvent {}
```

### Ciclo de vida: producción y recolección

Los eventos se **acumulan en el Aggregate Root** y se **publican en la capa de aplicación** tras persistir. El dominio nunca publica eventos directamente.

```java
// order/domain/model/Order.java
public class Order {
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    public void confirm() {
        if (this.status != OrderStatus.PENDING)
            throw new IllegalArgumentException("Order must be PENDING to confirm, was: " + this.status);
        this.status = OrderStatus.CONFIRMED;
        domainEvents.add(new OrderConfirmed(this.id, Instant.now()));
    }

    public void ship(TrackingNumber trackingNumber) {
        this.status = OrderStatus.SHIPPED;
        domainEvents.add(new OrderShipped(this.id, trackingNumber, Instant.now()));
    }

    public List<OrderEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

```java
// order/application/command/ShipOrderHandler.java
public class ShipOrderHandler implements ShipOrderUseCase {

    private final OrderRepositoryPort repository;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    public void execute(ShipOrderCommand command) {
        Order order = repository.findById(command.orderId()).orElseThrow(...);
        order.ship(command.trackingNumber());
        repository.save(order);
        order.pullDomainEvents().forEach(eventPublisher::publish);  // siempre después del save
    }
}
```

### Estrategia de publicación

- **Síncrona (dentro de la transacción):** Output Port `DomainEventPublisherPort`. La implementación decide el mecanismo (Spring Events, Kafka, etc.).
- **Outbox Pattern** para eventos críticos (garantía de al menos una entrega): persistir el evento en la misma transacción que el aggregate, publicar asíncronamente con un poller o CDC.
- **Nunca** publicar eventos antes de confirmar la persistencia del aggregate.

---

## CQRS: Separación Command / Query

### Principio

- **Command side:** modifica estado, usa el modelo de dominio rico, trabaja con Aggregates.
- **Query side:** solo lectura, usa proyecciones planas optimizadas (JDBC, JPQL con `@Query`, vistas), sin pasar por el modelo de dominio.
- Los dos lados comparten la misma base de datos (CQRS lógico), no infraestructura separada, salvo que escale a necesitarlo.

### Estructura en `application/`

```
application/
├── port/input/
│   ├── command/               ← Un interface por caso de uso de escritura
│   │   ├── PlaceOrderUseCase.java
│   │   └── ShipOrderUseCase.java
│   └── query/                 ← Un interface por caso de uso de lectura
│       ├── GetOrderByIdUseCase.java
│       └── ListOrdersByCustomerUseCase.java
├── command/                   ← Handlers de escritura
│   ├── PlaceOrderHandler.java
│   └── ShipOrderHandler.java
└── query/                     ← Handlers de lectura
    ├── GetOrderByIdHandler.java
    └── ListOrdersByCustomerHandler.java
```

### Query side: sin Aggregates

Los Query Handlers no cargan Aggregates. Trabajan directamente con proyecciones.

```java
// order/application/port/output/OrderQueryPort.java
public interface OrderQueryPort {
    Optional<OrderDetailView> findDetailById(OrderId id);
    Page<OrderSummaryView> findByCustomer(CustomerId customerId, Pageable pageable);
}

// order/application/dto/OrderDetailView.java
public record OrderDetailView(String orderId, String customerName, BigDecimal total,
                               String status, List<OrderLineView> lines) {}
```

La implementación en infraestructura puede usar JDBC, `@Query` con proyecciones de Spring Data o cualquier mecanismo optimizado, sin tocar el modelo de dominio.

---

## Gestión de Transacciones

`@Transactional` no existe en el dominio ni en la capa de aplicación. Toda gestión transaccional vive en infraestructura.

### Patrón: decorador transaccional en infraestructura

```java
// order/infrastructure/config/OrderConfig.java
@Configuration
public class OrderConfig {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepositoryPort repository,
                                               DomainEventPublisherPort publisher,
                                               MeterRegistry meterRegistry,
                                               PlatformTransactionManager txManager) {
        var handler = new PlaceOrderHandler(repository, publisher, meterRegistry);
        return new TransactionalPlaceOrderUseCase(handler, txManager);
    }
}
```

```java
// order/infrastructure/config/TransactionalPlaceOrderUseCase.java
public class TransactionalPlaceOrderUseCase implements PlaceOrderUseCase {

    private final PlaceOrderUseCase delegate;
    private final PlatformTransactionManager txManager;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        return new TransactionTemplate(txManager).execute(status -> delegate.execute(command));
    }
}
```

**Alternativa aceptable:** `@Transactional` únicamente en métodos del **JPA Adapter** cuando la transacción no necesita abarcar más de una operación de persistencia.

**Nunca:** `@Transactional` en clases de `application/` ni de `domain/`.

---

## Jerarquía de Excepciones

Una jerarquía consistente garantiza que el `GlobalExceptionHandler` mapee siempre el HTTP status correcto sin ambigüedad.

### Categorías y HTTP status mapping

```
DomainException (RuntimeException)
├── NotFoundException          → 404 Not Found
├── BusinessRuleViolationException → 422 Unprocessable Entity
├── ConflictException          → 409 Conflict
└── UnauthorizedException      → 403 Forbidden

ApplicationException (RuntimeException)
└── UseCaseValidationException → 400 Bad Request

InfrastructureException (RuntimeException)
└── ExternalServiceException   → 502 Bad Gateway
```

### Implementación base en el dominio

```java
// shared/domain/exception/DomainException.java
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}

// shared/domain/exception/NotFoundException.java
public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message) { super(message); }
}

// shared/domain/exception/BusinessRuleViolationException.java
public abstract class BusinessRuleViolationException extends DomainException {
    protected BusinessRuleViolationException(String message) { super(message); }
}

// shared/domain/exception/ConflictException.java
public abstract class ConflictException extends DomainException {
    protected ConflictException(String message) { super(message); }
    protected ConflictException(String message, Throwable cause) { super(message, cause); }
}

// shared/domain/exception/UnauthorizedException.java
public abstract class UnauthorizedException extends DomainException {
    protected UnauthorizedException(String message) { super(message); }
}
```

### Excepciones concretas de dominio extienden la categoría correcta

```java
// order/domain/exception/OrderNotFoundException.java
public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}

// order/domain/exception/InsufficientStockException.java
public class InsufficientStockException extends BusinessRuleViolationException {
    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d available=%d"
            .formatted(productId, requested, available));
    }
}

// order/domain/exception/OrderConcurrentModificationException.java
public class OrderConcurrentModificationException extends ConflictException {
    public OrderConcurrentModificationException(OrderId id, Throwable cause) {
        super("Order modified concurrently: " + id, cause);  // cause se propaga correctamente
    }
}

// order/domain/exception/UnauthorizedCancellationException.java
public class UnauthorizedCancellationException extends UnauthorizedException {
    public UnauthorizedCancellationException(UserId requestedBy, OrderId orderId) {
        super("User %s is not authorized to cancel order %s".formatted(requestedBy, orderId));
    }
}
```

### GlobalExceptionHandler mapeado por categoría

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return problem(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalService(ExternalServiceException ex) {
        log.error("External service failure", ex);
        return problem(HttpStatus.BAD_GATEWAY, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create("https://api.myapp.com/errors/" +
            ex.getClass().getSimpleName().toLowerCase().replace("exception", "")));
        return problem;
    }
}
```

---

## Migraciones de Base de Datos: Flyway

- **`ddl-auto` siempre en `validate` o `none` en producción.** Nunca `create`, `create-drop` ni `update` fuera de tests.
- Todas las migraciones en `src/main/resources/db/migration/`.
- Nomenclatura: `V{version}__{descripcion_en_snake_case}.sql` → `V1__create_orders_table.sql`.
- Las migraciones son **inmutables** una vez aplicadas. Para corregir errores, nueva migración.
- En tests con Testcontainers, Flyway se ejecuta automáticamente — no usar `@Sql` para esquema.

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
  jpa:
    hibernate:
      ddl-auto: validate
```

---

## Convenciones de API REST

### Problem Details (RFC 9457)

Todos los errores devuelven `application/problem+json`. Usar `ProblemDetail` de Spring 6+. La implementación completa del `GlobalExceptionHandler` está en la sección [Jerarquía de Excepciones](#jerarquía-de-excepciones) — captura siempre por **categoría base**, nunca por excepción concreta.

### Versionado de API

- Versionar en la URL: `/api/v1/orders`, `/api/v2/orders`.
- Una versión por `@RequestMapping` de clase en el controller.
- Nunca versionar por header o query param.
- Mantener la versión anterior al menos un ciclo de release tras deprecarla.

### Paginación

Nunca devolver un array crudo para colecciones paginadas. Respuesta estandarizada:

```java
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
```

### Convenciones generales

- `POST` para crear → `201 Created` + `Location` header.
- `PUT` para reemplazar completo, `PATCH` para actualización parcial.
- `DELETE` exitoso → `204 No Content`.
- IDs en la URL siempre como `String` (UUID como string), nunca claves numéricas internas.
- Validaciones Bean Validation únicamente en DTOs de infraestructura (request), nunca en dominio.

---

## Observabilidad

### Structured Logging

- **Nunca concatenar strings en logs.** Usar siempre parámetros con `{}`.
- Logs en **formato JSON en producción** (Logback + `logstash-logback-encoder`).
- Incluir siempre `traceId` y `spanId` (propagados automáticamente por Micrometer Tracing vía MDC).
- Nivel por defecto `INFO`. `DEBUG` solo en desarrollo, nunca habilitado por defecto en producción.

```java
// ✅
log.info("Order placed orderId={} customerId={}", order.getId(), command.customerId());

// ❌
log.info("Order " + orderId + " placed for customer " + customerId);
```

### Métricas con Micrometer

- Registrar métricas de negocio en los Command/Query Handlers, no en infraestructura.
- `MeterRegistry` inyectado como dependencia desde config.
- Nombrar métricas en snake_case con prefijo de contexto: `orders.placed.total`, `orders.cancelled.total`.

```java
// order/application/command/PlaceOrderHandler.java (extracto — campos completos en Desacoplamiento de Spring)
public class PlaceOrderHandler implements PlaceOrderUseCase {
    private final MeterRegistry meterRegistry;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        var orderId = // ... lógica
        meterRegistry.counter("orders.placed.total").increment();
        return orderId;
    }
}
```

### Tracing con OpenTelemetry

- Configurar exclusivamente en `shared/infrastructure/config/`.
- Los spans se propagan automáticamente con Micrometer Tracing + Spring Boot Actuator.
- Añadir atributos de negocio relevantes al span activo en los handlers.

### Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized
```

---

## Calidad de Código: Tooling

### Spotless (formato automático)

```kotlin
spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
    }
}
```

- `./gradlew spotlessCheck` en CI — falla el build si el formato no es correcto.
- `./gradlew spotlessApply` para formatear localmente antes de commit.

### Checkstyle

Reglas mínimas a activar:
- Sin imports con `*`.
- Javadoc obligatorio en interfaces públicas de puertos (`port/input/` y `port/output/`).
- Longitud máxima de línea: 120 caracteres.
- Sin bloques `catch` vacíos.

### ArchUnit: Tests de Arquitectura Obligatorios

```java
@AnalyzeClasses(packages = "com.{ctx}")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsIsolated =
        noClasses().that().resideInAPackage("..domain..")
                   .should().dependOnClassesThat()
                   .resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                                       "..application..", "..infrastructure..");

    @ArchTest
    static final ArchRule applicationDoesNotUseInfrastructure =
        noClasses().that().resideInAPackage("..application..")
                   .should().dependOnClassesThat()
                   .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule springAnnotationsOnlyInInfrastructure =
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
                   .should().beAnnotatedWith(Service.class)
                   .orShould().beAnnotatedWith(Component.class)
                   .orShould().beAnnotatedWith(Repository.class)
                   .orShould().beAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule commandHandlersImplementCommandPorts =
        classes().that().resideInAPackage("..application.command..")
                 .should().implement(resideInAPackage("..application.port.input.command.."));

    @ArchTest
    static final ArchRule queryHandlersImplementQueryPorts =
        classes().that().resideInAPackage("..application.query..")
                 .should().implement(resideInAPackage("..application.port.input.query.."));

    // Reglas de aislamiento entre features:
    // ninguna feature importa el dominio de otra feature directamente
    @ArchTest
    static final ArchRule orderDoesNotImportProductDomain =
        noClasses().that().resideInAPackage("..order..")
                   .should().dependOnClassesThat()
                   .resideInAPackage("..product..domain..");

    @ArchTest
    static final ArchRule featuresDoNotImportSharedApplication =
        noClasses().that().resideInAPackage("..order..", "..product..", "..customer..")
                   .should().dependOnClassesThat()
                   .resideInAPackage("..shared..application..");
}
```

---

## Secrets en CI y entorno local

### Reglas generales

- **Nunca** commitear un secret. Ni en `application.yml`, ni en `.env`, ni en el historial de Git.
- El repositorio incluye siempre un `.env.example` con las claves necesarias pero **sin valores reales** — es el contrato de configuración del proyecto.
- `.env` está en `.gitignore` desde el commit inicial.

```bash
# .env.example  — commitear esto
DATABASE_URL=jdbc:postgresql://localhost:5432/myapp
DATABASE_USERNAME=
DATABASE_PASSWORD=
PAYMENT_API_URL=https://sandbox.payment-provider.com
PAYMENT_API_KEY=
JWT_SECRET=

# .env  — NUNCA commitear esto (está en .gitignore)
DATABASE_URL=jdbc:postgresql://localhost:5432/myapp
DATABASE_USERNAME=myapp_user
DATABASE_PASSWORD=s3cr3t
PAYMENT_API_KEY=sk_live_abc123
```

### Secrets en GitHub Actions

Los secrets se definen en **Settings → Secrets and variables → Actions** del repositorio y se referencian en el workflow con `${{ secrets.NOMBRE }}`. **Nunca** se imprimen en los logs.

```yaml
- name: Integration tests
  run: ./gradlew test --tests "..infrastructure..*"
  env:
    DATABASE_URL: ${{ secrets.TEST_DATABASE_URL }}    # inyectado, nunca en texto plano
    PAYMENT_API_KEY: ${{ secrets.PAYMENT_API_KEY }}
```

### Nunca loguear secrets

```java
// ❌
log.info("Connecting to payment API with key={}", apiKey);

// ✅
log.info("Connecting to payment API baseUrl={}", properties.baseUrl());
// la apiKey nunca aparece en logs
```

### Secrets de runtime vs secrets de CI

| Tipo | Dónde viven | Ejemplo |
|------|-------------|---------|
| Runtime (producción) | Vault / AWS Secrets Manager / env vars del host | `DATABASE_PASSWORD`, `JWT_SECRET` |
| CI (build/test) | GitHub Actions Secrets | `TEST_DATABASE_URL`, credenciales de registry |
| Locales (desarrollo) | `.env` (en `.gitignore`) | Cualquiera de los anteriores en local |

---

## Desacoplamiento de Spring: Registro de Beans

El dominio y la aplicación **no tienen ninguna anotación Spring**. Todo se registra desde `{feature}/infrastructure/config/`.

```java
// order/infrastructure/config/OrderConfig.java
@Configuration
public class OrderConfig {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepositoryPort repository,
                                               DomainEventPublisherPort publisher,
                                               MeterRegistry meterRegistry,
                                               PlatformTransactionManager txManager) {
        var handler = new PlaceOrderHandler(repository, publisher, meterRegistry);
        return new TransactionalPlaceOrderUseCase(handler, txManager);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderQueryPort queryPort) {
        return new GetOrderByIdHandler(queryPort);
    }
}
```

---

## Nomenclatura

| Elemento                  | Convención                                   | Ejemplo                          |
|---------------------------|----------------------------------------------|----------------------------------|
| Entidad / Aggregate Root  | Sustantivo singular del dominio              | `Order`, `Product`               |
| Value Object              | Sustantivo descriptivo del concepto          | `Money`, `TrackingNumber`        |
| Domain Event              | Sustantivo + participio pasado               | `OrderPlaced`, `OrderShipped`    |
| Domain Service            | Sustantivo + "Service"                       | `PricingService`                 |
| Domain Exception          | Sustantivo + "Exception"                     | `InsufficientStockException`     |
| Command                   | Verbo imperativo + "Command"                 | `PlaceOrderCommand`              |
| Query                     | "Find/Get/List" + sustantivo + "Query"       | `FindOrderByIdQuery`             |
| Command Use Case (port)   | Verbo imperativo + "UseCase"                 | `PlaceOrderUseCase`              |
| Query Use Case (port)     | "Get/List/Find" + sustantivo + "UseCase"     | `GetOrderByIdUseCase`            |
| Command Handler           | Verbo + "Handler"                            | `PlaceOrderHandler`              |
| Query Handler             | "Get/List/Find" + sustantivo + "Handler"     | `GetOrderByIdHandler`            |
| Response / View           | Sustantivo + "Response" / "View"             | `OrderDetailView`                |
| Output Port (repo)        | Sustantivo + "RepositoryPort"                | `OrderRepositoryPort`            |
| Output Port (query)       | Sustantivo + "QueryPort"                     | `OrderQueryPort`                 |
| Output Port (events)      | Sustantivo + "PublisherPort"                 | `DomainEventPublisherPort`       |
| JPA Entity                | Sustantivo + "Entity"                        | `OrderEntity`                    |
| Spring Data Repository    | "Spring" + Sustantivo + "Repository"         | `SpringOrderRepository`          |
| JPA Adapter               | Sustantivo + "JpaAdapter"                    | `OrderJpaAdapter`                |
| REST Controller           | Sustantivo + "Controller"                    | `OrderController`                |
| Mapper de persistencia    | Sustantivo + "PersistenceMapper"             | `OrderPersistenceMapper`         |
| @Configuration de beans   | Sustantivo + "Config"                        | `OrderConfig`                    |

---

## Testing

### Pirámide de Tests

```
           /‾‾‾‾‾‾‾‾‾‾‾\
          /  E2E / IT    \      ← Testcontainers + Spring Boot Test (pocos, lentos)
         /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
        /  Integration     \   ← @WebMvcTest, @DataJpaTest (capa a capa)
       /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
      /   Unit Tests         \  ← JUnit 5 + Mockito (mayoría — dominio sin mocks)
     /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
    /  Architecture Tests      \ ← ArchUnit (siempre en CI)
   /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
```

### Convenciones

- Patrón **Given / When / Then** con comentarios en todos los tests.
- Nombre: `should{Resultado}_when{Condicion}()`.
- Tests de dominio **sin Mockito** — el dominio es puro y testeable sin mocks.
- Tests de Command/Query Handlers mockean los Output Ports.
- `@WebMvcTest` para controllers — nunca levantar el contexto completo para tests REST.
- `@DataJpaTest` + Testcontainers para los JPA Adapters.
- Nunca `@SpringBootTest` para tests unitarios.

---

## ObjectMother: fixtures de test

Sin una estrategia de fixtures, cada test construye sus propios objetos con parámetros arbitrarios. El resultado son tests frágiles y ruidosos donde el dato del test eclipsa la intención del test. El patrón **ObjectMother** centraliza la construcción de objetos de test por feature.

### Estructura: un ObjectMother por Aggregate, junto a sus tests

```
src/test/java/com/{ctx}/
└── order/
    ├── domain/
    │   ├── model/
    │   │   └── OrderTest.java
    │   └── OrderMother.java          ← fixtures de Order para toda la feature
    ├── application/
    │   └── command/
    │       └── PlaceOrderHandlerTest.java
    └── infrastructure/
        └── adapter/
            └── input/rest/
                └── OrderControllerTest.java
```

### Implementación: métodos de fábrica semánticos

El nombre del método describe el **estado de negocio**, no los datos técnicos.

```java
// order/domain/OrderMother.java
public final class OrderMother {

    private OrderMother() {}

    /** Pedido recién creado, pendiente de confirmación */
    public static Order pending() {
        return Order.create(
            CustomerIdMother.any(),
            List.of(OrderLineMother.withProduct(ProductIdMother.any(), 2))
        );
    }

    /** Pedido confirmado y listo para enviar */
    public static Order confirmed() {
        var order = pending();
        order.confirm();
        return order;
    }

    /** Pedido ya enviado con número de seguimiento */
    public static Order shipped() {
        var order = confirmed();
        order.ship(TrackingNumberMother.any());
        return order;
    }

    /** Pedido cancelado por el cliente */
    public static Order cancelledByCustomer() {
        var order = pending();
        // cancel() requiere (UserId requestedBy, CancellationReason reason)
        order.cancel(UserId.of(UUID.randomUUID()), CancellationReason.CUSTOMER_REQUEST);
        return order;
    }

    /** Pedido con un estado personalizado para tests específicos */
    public static Order withStatus(OrderStatus status) {
        return switch (status) {
            case PENDING    -> pending();
            case CONFIRMED  -> confirmed();
            case SHIPPED    -> shipped();
            case CANCELLED  -> cancelledByCustomer();
        };
    }
}
```

```java
// shared/domain/CustomerIdMother.java  — Value Objects en shared/
public final class CustomerIdMother {
    public static CustomerId any() {
        return CustomerId.of(UUID.randomUUID());
    }
    public static CustomerId fixed() {
        return CustomerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}

// order/domain/OrderIdMother.java
public final class OrderIdMother {
    public static OrderId any() {
        return OrderId.of(UUID.randomUUID());
    }
}

// order/domain/TrackingNumberMother.java
public final class TrackingNumberMother {
    public static TrackingNumber any() {
        return new TrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}

// order/domain/OrderLineMother.java
public final class OrderLineMother {
    private static final Currency EUR = Currency.getInstance("EUR");

    public static OrderLineCommand withProduct(ProductId productId, int quantity) {
        return new OrderLineCommand(productId, quantity, new Money(new BigDecimal("9.99"), EUR));
    }
}

// product/domain/ProductIdMother.java
public final class ProductIdMother {
    public static ProductId any() {
        return ProductId.of(UUID.randomUUID());
    }
}

// order/domain/exception/OrderAlreadyShippedException.java
public class OrderAlreadyShippedException extends BusinessRuleViolationException {
    public OrderAlreadyShippedException(OrderId id) {
        super("Order already shipped, cannot be cancelled: " + id);
    }
}
```

### Uso en tests: la intención queda en primer plano

```java
@Test
void shouldThrowException_whenCancellingShippedOrder() {
    // Given
    var order = OrderMother.shipped();   // ← intención clara en una línea

    // When / Then
    assertThatThrownBy(() -> order.cancel(UserId.of(UUID.randomUUID()), CancellationReason.CUSTOMER_REQUEST))
        .isInstanceOf(OrderAlreadyShippedException.class);
}

@Test
void shouldReturnNotFound_whenOrderDoesNotExist() {
    // Given
    var orderId = OrderIdMother.any();
    when(repository.findById(orderId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(orderId))
        .isInstanceOf(OrderNotFoundException.class);
}
```

### Reglas del ObjectMother

- Un `*Mother` por Aggregate o Value Object complejo.
- Los métodos describen **estados de negocio**, nunca parámetros técnicos (`withId(uuid)` está bien; `withStatus(PENDING)` también, como método de escape).
- Los `*Mother` de Value Objects compartidos van en `shared/` (p.ej. `CustomerIdMother`, `MoneyMother`).
- Los `*Mother` de Aggregates van en la carpeta de test de su feature.
- **Nunca** usar `*Mother` en código de producción.

---

## Conventional Commits: formato de mensajes

Todo commit sigue la especificación [Conventional Commits](https://www.conventionalcommits.org). Esto permite generar CHANGELOG automático y hace el historial legible como documentación.

### Formato

```
<tipo>(<scope>): <descripción en imperativo, minúsculas, sin punto final>

[cuerpo opcional — explica el POR QUÉ, no el qué]

[footer opcional — referencias a issues, breaking changes]
```

### Tipos permitidos

| Tipo | Cuándo usarlo |
|------|---------------|
| `feat` | Nueva funcionalidad de negocio |
| `fix` | Corrección de bug |
| `refactor` | Cambio de código sin modificar comportamiento |
| `test` | Añadir o corregir tests |
| `docs` | Solo documentación |
| `chore` | Tareas de mantenimiento (deps, config, CI) |
| `perf` | Mejora de rendimiento |
| `revert` | Revertir un commit anterior |

### Scope: nombre de la feature afectada

```bash
feat(order): add cancellation by customer request
fix(product): correct stock calculation on concurrent updates
refactor(shared): extract Money value object from Order
test(customer): add ObjectMother for CustomerAggregate
chore(deps): bump Spring Boot to 4.0.3
```

### Breaking changes

```bash
feat(order)!: replace OrderId Long with UUID

BREAKING CHANGE: OrderId is now UUID. Migrate existing data with V5__migrate_order_ids.sql
```

### Reglas

- La descripción en **imperativo** y **minúsculas**: `add`, `fix`, `remove` — nunca `Added`, `Fixes`, `Removed`.
- Máximo **72 caracteres** en la primera línea.
- El cuerpo explica **por qué**, no qué — el diff ya muestra el qué.
- Un commit = un cambio cohesionado. No mezclar feat + refactor en el mismo commit.

---

## Pre-commit Hooks

Los hooks ejecutan spotless y checkstyle automáticamente antes de cada commit. Así los errores de formato se detectan en local, no en CI.

### Setup con script de inicialización

```bash
# scripts/setup-hooks.sh  — ejecutar una vez tras clonar el repo
#!/bin/bash
set -e

HOOKS_DIR=".git/hooks"
PRE_COMMIT="$HOOKS_DIR/pre-commit"

cat > "$PRE_COMMIT" << 'EOF'
#!/bin/bash
# No usar set -e aquí: necesitamos capturar el exit code de cada paso manualmente
echo "▶ Running pre-commit checks..."

echo "  → Spotless format check"
./gradlew spotlessCheck --daemon -q
if [ $? -ne 0 ]; then
  echo "  ✗ Format errors found. Run: ./gradlew spotlessApply"
  exit 1
fi

echo "  → Checkstyle"
./gradlew checkstyleMain --daemon -q
if [ $? -ne 0 ]; then
  echo "  ✗ Checkstyle violations found."
  exit 1
fi

echo "  ✓ Pre-commit checks passed"
EOF

chmod +x "$PRE_COMMIT"
echo "✓ Git hooks installed"
```

```bash
# Ejecutar una vez tras clonar
chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh
```

### Reglas

- El hook usa `--daemon` para reutilizar el proceso Gradle y mantenerlo rápido (~3-5s).
- **Nunca** ejecutar los tests completos en el hook — son responsabilidad del CI, no del commit.
- Si el formato tiene errores, el hook muestra el comando exacto para corregirlo: `./gradlew spotlessApply`.
- El script de setup se incluye en el repositorio en `scripts/` y se documenta en el `README.md`.

---

## Git: Branch Strategy

Para un desarrollador en solitario la estrategia óptima es **trunk-based development simplificado**: ramas de feature cortas que se mergean a `main` frecuentemente. Evita la complejidad de Gitflow sin sacrificar estabilidad.

### Ramas permanentes

| Rama | Propósito |
|------|-----------|
| `main` | Siempre desplegable a producción. Protegida. |
| `develop` | Integración continua. Base para features. |

### Ramas temporales (vida máxima: 2 días)

```
feature/<issue-id>-<descripcion-kebab-case>   → feat(order): ...
fix/<issue-id>-<descripcion-kebab-case>        → fix(product): ...
refactor/<descripcion-kebab-case>              → refactor(shared): ...
chore/<descripcion-kebab-case>                 → chore(deps): ...
hotfix/<issue-id>-<descripcion-kebab-case>     → sale de main, mergea a main y develop
```

**Ejemplos reales:**
```
feature/42-order-cancellation
fix/87-concurrent-stock-update
hotfix/103-payment-timeout-production
```

### Flujo normal

```
develop → feature/X → PR → CI verde → merge a develop → PR → CI verde → merge a main → deploy
```

### Flujo hotfix

```
main → hotfix/X → PR → CI verde → merge a main → deploy → cherry-pick a develop
```

### Reglas

- Las ramas de feature nunca duran más de 2 días — si duran más, hay un problema de scope.
- `main` **nunca** recibe push directo. Todo pasa por PR.
- El nombre de la rama referencia siempre el issue/ticket cuando existe.

---

## Git: Branch Protection y PR Template

### Branch protection rules (GitHub Settings → Branches)

Configurar en `main` y `develop`:

```
✅ Require a pull request before merging
   ✅ Require approvals: 0  (solo dev — el CI es suficiente guardia)

✅ Require status checks to pass before merging
   Status checks requeridos:
   - Format check (spotlessCheck)
   - Style check (checkstyle)
   - Unit tests
   - Architecture tests
   - Integration tests

✅ Require branches to be up to date before merging
✅ Do not allow bypassing the above settings
✅ Restrict who can push to matching branches → solo tú
```

### PR Template

```markdown
<!-- .github/pull_request_template.md -->

## ¿Qué hace este PR?
<!-- Una línea: qué problema resuelve o qué funcionalidad añade -->

## Motivación / contexto
<!-- Por qué es necesario este cambio. Link al issue si existe: Closes #XX -->

## Tipo de cambio
- [ ] `feat` — nueva funcionalidad
- [ ] `fix` — corrección de bug
- [ ] `refactor` — sin cambio de comportamiento
- [ ] `chore` — mantenimiento, dependencias, CI
- [ ] `docs` — solo documentación

## Checklist
- [ ] Tests añadidos o actualizados
- [ ] ArchUnit sigue pasando (`./gradlew test --tests "*ArchitectureTest"`)
- [ ] Sin anotaciones Spring en `domain/` ni `application/`
- [ ] Migraciones Flyway añadidas si hay cambios de esquema
- [ ] `.env.example` actualizado si hay nuevas variables de entorno

## Notas para el revisor
<!-- Decisiones de diseño relevantes, trade-offs, alternativas descartadas -->
```

---

## Seguridad: Scanning en CI

### OWASP Dependency Check

Detecta dependencias con vulnerabilidades CVE conocidas. Se ejecuta en CI antes de cualquier despliegue.

Añadir dentro del bloque `[plugins]` existente en `libs.versions.toml`:
```toml
owasp-dependency-check = { id = "org.owasp.dependencycheck", version = "9.x" }
```

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.owaspDependencyCheck)
}

dependencyCheck {
    failBuildOnCVSS = 7.0f          // falla si hay CVE con score ≥ 7 (High/Critical)
    suppressionFile = "config/owasp-suppressions.xml"  // falsos positivos documentados
    format = "HTML"
    outputDirectory = "build/reports/dependency-check"
}
```

```yaml
# En el workflow CI — paso previo al despliegue
- name: OWASP Dependency Check
  run: ./gradlew dependencyCheckAnalyze
  continue-on-error: false

- name: Upload OWASP report
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: owasp-report
    path: build/reports/dependency-check/
    retention-days: 30
```

### Trivy: escaneo de imagen Docker

Después de construir la imagen Docker, Trivy la escanea buscando vulnerabilidades en el OS base y las dependencias.

```yaml
- name: Build Docker image
  run: docker build -t myapp:${{ github.sha }} .

- name: Scan Docker image with Trivy
  uses: aquasecurity/trivy-action@0.20.0
  with:
    image-ref: myapp:${{ github.sha }}
    format: sarif
    output: trivy-results.sarif
    severity: HIGH,CRITICAL
    exit-code: '1'               # falla el build si hay HIGH o CRITICAL

- name: Upload Trivy results to GitHub Security
  if: always()
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: trivy-results.sarif
```

---

## Versionado Semántico y Release

### Estrategia: tags de Git + CHANGELOG automático

La versión sigue [SemVer](https://semver.org): `MAJOR.MINOR.PATCH`.

| Incremento | Cuándo |
|------------|--------|
| `PATCH` | `fix:` commits desde el último release |
| `MINOR` | Al menos un `feat:` commit desde el último release |
| `MAJOR` | Al menos un `BREAKING CHANGE` desde el último release |

### Crear un release

```bash
# 1. Asegurarse de estar en main con CI verde
git checkout main && git pull

# 2. Tag semántico (el CHANGELOG se genera del historial de Conventional Commits)
git tag -a v1.2.0 -m "chore(release): v1.2.0"
git push origin v1.2.0
```

### Workflow de release automático

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags: ['v*.*.*']

permissions:
  contents: write    # necesario para crear el release en GitHub
  packages: write    # necesario para publicar imagen Docker

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0    # necesario para generar CHANGELOG desde todo el historial

      - uses: gradle/actions/setup-gradle@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Run full CI
        run: ./gradlew spotlessCheck checkstyleMain test

      - name: Build Docker image
        run: |
          docker build -t ghcr.io/${{ github.repository }}:${{ github.ref_name }} .
          docker build -t ghcr.io/${{ github.repository }}:latest .

      - name: Push Docker image to GHCR
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}:${{ github.ref_name }}
          docker push ghcr.io/${{ github.repository }}:latest

      - name: Generate CHANGELOG and create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true    # genera notas desde Conventional Commits
          token: ${{ secrets.GITHUB_TOKEN }}
```

---

## Dockerfile: convenciones

### Multi-stage build obligatorio

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copiar solo los archivos de dependencias primero (mejor cache de capas)
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon -q

# Copiar el código fuente y construir
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

# Non-root user — nunca ejecutar como root en producción
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiar solo el JAR del stage de build
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Puerto documentado (no publicado — eso lo decide docker-compose o k8s)
EXPOSE 8080

# Health check embebido en la imagen
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### Reglas del Dockerfile

- **Siempre multi-stage**: el stage `builder` contiene el JDK completo, el `runtime` solo el JRE.
- **Non-root user** (`appuser`) — obligatorio. Nunca `USER root` en runtime.
- **`eclipse-temurin:25-jre-alpine`** como base del runtime — imagen mínima, menos superficie de ataque.
- `-XX:+UseContainerSupport` — permite a la JVM respetar los límites de memoria del contenedor.
- `-XX:MaxRAMPercentage=75.0` — la JVM usa máximo el 75% de la RAM asignada al contenedor.
- Nunca hardcodear variables de entorno en el `Dockerfile` — se inyectan en runtime.

---

## Estrategia de Despliegue

### Health check y verificación post-deploy

Antes de dar el despliegue por bueno, verificar que el endpoint de health de Actuator responde correctamente.

```yaml
# En el workflow de release, después del push de imagen
- name: Deploy to staging
  run: |
    # (aquí va el comando de deploy específico: kubectl, docker-compose, etc.)
    echo "Deploying ${{ github.ref_name }} to staging..."

- name: Smoke test — verify health endpoint
  run: |
    echo "Waiting for application to start..."
    for i in {1..12}; do
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://staging.myapp.com/actuator/health)
      if [ "$STATUS" = "200" ]; then
        echo "✓ Application healthy (attempt $i)"
        exit 0
      fi
      echo "  Attempt $i: status=$STATUS, retrying in 10s..."
      sleep 10
    done
    echo "✗ Application failed to become healthy after 120s"
    exit 1
```

### Zero-downtime con rolling deploy

Configurar siempre al menos **2 réplicas** en producción. El orquestador (Docker Swarm, Kubernetes, Railway, Fly.io…) reemplaza réplicas de una en una esperando que la nueva esté healthy antes de terminar la antigua.

```yaml
# docker-compose.yml (producción simplificada)
services:
  app:
    image: ghcr.io/myorg/myapp:${APP_VERSION}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1          # actualizar de una en una
        delay: 30s              # esperar 30s entre réplicas
        failure_action: rollback
        order: start-first      # arrancar nueva antes de parar la vieja (zero-downtime)
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 60s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: ${DATABASE_URL}
      JWT_SECRET: ${JWT_SECRET}
```

### Estrategia de rollback

Si el smoke test falla o se detecta un problema en producción:

```bash
# Rollback inmediato a la versión anterior
git tag -a v1.1.1-hotfix -m "revert: rollback to v1.1.0 due to issue #XXX"
git push origin v1.1.1-hotfix

# O directamente con docker
docker service update --image ghcr.io/myorg/myapp:v1.1.0 myapp_app
```

### Reglas de despliegue

- **Nunca desplegar en viernes** salvo hotfix urgente.
- Toda nueva versión pasa primero por `staging` — si el smoke test falla en staging, no llega a producción.
- Las migraciones Flyway se ejecutan automáticamente al arrancar. Si fallan, la app no arranca — es un fallo visible y limpio.
- Si una migración puede ser destructiva (drop column, rename), dividirla en dos releases: primero deploy sin la migración destructiva, luego deploy con ella una vez confirmado que nada la usa.

---

## Seguridad: Spring Security sin contaminar el dominio

El dominio y la aplicación **nunca conocen a Spring Security**. El contexto de seguridad es un detalle de infraestructura.

### Principio: el dominio recibe identidad, no contexto de seguridad

El dominio trabaja con Value Objects de identidad propios (`UserId`, `TenantId`), nunca con `Authentication`, `Principal`, `SecurityContext` ni `UserDetails`.

```java
// ❌ Spring Security infiltrado en el dominio
public class OrderService {
    public void placeOrder(PlaceOrderCommand cmd) {
        var user = SecurityContextHolder.getContext().getAuthentication(); // NUNCA
    }
}

// ✅ El dominio recibe lo que necesita como parámetro
public record PlaceOrderCommand(CustomerId customerId, List<OrderLineCommand> lines, UUID idempotencyKey) {}
```

### Patrón: resolver la identidad en el adaptador de entrada

El controller (infraestructura) extrae la identidad del `SecurityContext` y la convierte a un Value Object del dominio antes de construir el Command o Query.

```java
// order/infrastructure/adapter/input/rest/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final AuthenticatedUserResolver userResolver;  // helper de infraestructura

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request,
                                               Authentication authentication) {
        var customerId = userResolver.resolveCustomerId(authentication);  // conversión aquí
        var command = new PlaceOrderCommand(customerId, request.lines(), null);  // sin idempotencia en este endpoint
        var orderId = placeOrderUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + orderId)).build();
    }
}
```

```java
// order/infrastructure/adapter/input/rest/AuthenticatedUserResolver.java
@Component
public class AuthenticatedUserResolver {

    public CustomerId resolveCustomerId(Authentication authentication) {
        var subject = (String) authentication.getPrincipal();
        return CustomerId.of(UUID.fromString(subject));
    }
}
```

### Autorización: en infraestructura, no en el dominio

- Usar `@PreAuthorize` o `SecurityFilterChain` en la capa de infraestructura para autorización basada en roles.
- La autorización basada en **reglas de negocio** (p.ej. "solo el propietario puede cancelar su pedido") sí puede vivir en el dominio como lógica de negocio, pero recibiendo el `UserId` como parámetro, sin tocar `SecurityContext`.

```java
// ✅ Autorización de negocio en el dominio
public class Order {
    public void cancel(UserId requestedBy, CancellationReason reason) {
        if (!this.customerId.equals(requestedBy) && !this.assignedOperatorId.equals(requestedBy)) {
            throw new UnauthorizedCancellationException(requestedBy, this.id);
        }
        // ... lógica de cancelación
    }
}
```

### Configuración de Spring Security

- Toda la configuración de `SecurityFilterChain`, JWT, OAuth2, etc. en `shared/infrastructure/config/SecurityConfig.java`.
- Los beans de Spring Security **nunca** se inyectan fuera de `infrastructure/`.

```java
// shared/infrastructure/config/SecurityConfig.java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

---

## Diseño de Aggregates: Reglas de Oro

Estas reglas previenen los problemas más frecuentes en proyectos DDD a medida que el modelo crece.

### Regla 1: Referenciar otros Aggregates solo por ID

Un Aggregate nunca contiene una referencia directa a otro Aggregate. Solo guarda su ID como Value Object. La carga de Aggregates relacionados es responsabilidad de la capa de aplicación.

```java
// ❌ Referencia directa a otro Aggregate
public class Order {
    private Customer customer;  // Order no debe cargar Customer completo
}

// ✅ Referencia por ID
public class Order {
    private final CustomerId customerId;  // solo el ID
}
```

### Regla 2: Mantener los Aggregates pequeños

Un Aggregate debe contener **solo lo necesario para proteger sus invariantes**. Si un campo o colección no participa en ninguna regla de negocio del Aggregate, probablemente no pertenece ahí.

Señales de que un Aggregate es demasiado grande:
- Las transacciones tardan más de lo esperado.
- Los tests requieren construir objetos muy complejos.
- Múltiples casos de uso distintos modifican siempre el mismo Aggregate.

### Regla 3: Un Aggregate por transacción

Una transacción modifica **un solo Aggregate Root**. Si dos Aggregates deben cambiar juntos, la coordinación ocurre mediante Domain Events y consistencia eventual.

```java
// ❌ Dos Aggregates en la misma transacción
repository.save(order);
repository.save(inventory);  // transacción que abarca dos Aggregates

// ✅ Order emite evento, Inventory reacciona de forma eventual
order.confirm();
repository.save(order);
order.pullDomainEvents().forEach(eventPublisher::publish);
// InventoryReservationHandler escucha OrderConfirmed y actualiza Inventory en su propia transacción
```

### Regla 4: Los invariantes del Aggregate se protegen siempre en el Aggregate Root

Nunca permitir que código externo al Aggregate manipule directamente su estado interno o sus colecciones hijas.

```java
// ❌ El handler manipula directamente el estado interno
order.getLines().add(new OrderLine(...));   // rompe el encapsulamiento

// ✅ El Aggregate Root controla sus invariantes
order.addLine(productId, quantity, unitPrice);  // el Aggregate valida y modifica
```

---

## Idempotencia

Toda operación que pueda ser reintentada (por un cliente HTTP, por un consumer de mensajería, por Resilience4j) debe ser idempotente o estar protegida contra ejecuciones duplicadas.

### Idempotencia en Commands de API REST

Usar una clave de idempotencia enviada por el cliente en el header `Idempotency-Key`. El adaptador de entrada la extrae y la incluye en el Command. La capa de aplicación delega la comprobación a un Output Port.

```java
// order/infrastructure/adapter/input/rest/OrderController.java
@PostMapping
public ResponseEntity<OrderResponse> place(
        @Valid @RequestBody PlaceOrderRequest request,
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        Authentication authentication) {
    var customerId = userResolver.resolveCustomerId(authentication);  // resuelto desde Security
    var command = new PlaceOrderCommand(customerId, request.lines(), idempotencyKey);
    // ...
}
```

> El campo `userResolver` se inyecta como dependencia en el constructor del controller, igual que en el ejemplo de la sección [Seguridad](#seguridad-spring-security-sin-contaminar-el-dominio).

```java
// order/application/port/output/IdempotencyPort.java
public interface IdempotencyPort {
    Optional<OrderId> findProcessedCommand(UUID idempotencyKey);
    void markAsProcessed(UUID idempotencyKey, OrderId result);
}
```

```java
// order/application/command/PlaceOrderHandler.java
public class PlaceOrderHandler implements PlaceOrderUseCase {

    private final IdempotencyPort idempotency;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        return idempotency.findProcessedCommand(command.idempotencyKey())
                .orElseGet(() -> {
                    var orderId = processNewOrder(command);
                    idempotency.markAsProcessed(command.idempotencyKey(), orderId);
                    return orderId;
                });
    }
}
```

### Idempotencia en consumers de mensajería

Todo Message Consumer debe protegerse frente a mensajes duplicados. Patrón recomendado: tabla de mensajes procesados con el `messageId` como clave única.

```java
// order/infrastructure/adapter/input/messaging/OrderEventConsumer.java
@Slf4j
@Component
public class OrderEventConsumer {

    private final ProcessedMessageRepository processedMessages;
    private final ShipOrderUseCase shipOrderUseCase;

    public void onOrderReadyToShip(OrderReadyToShip event, String messageId) {
        if (processedMessages.existsById(messageId)) {
            log.info("Duplicate message ignored messageId={}", messageId);
            return;
        }
        shipOrderUseCase.execute(new ShipOrderCommand(event.orderId(), event.trackingNumber()));
        processedMessages.markProcessed(messageId);
    }
}
```

**Regla:** el `messageId` debe persistirse **en la misma transacción** que el efecto de negocio, nunca en una transacción separada.

---

## Configuración Externalizada (12-Factor)

### Principio

La configuración que varía entre entornos (URLs, credenciales, timeouts, feature flags) nunca vive en el código fuente. Siempre en variables de entorno o en un config server.

### `@ConfigurationProperties` solo en infraestructura

Encapsular la configuración en clases `@ConfigurationProperties` tipadas. **Nunca** inyectar `@Value` o `Environment` en dominio ni aplicación.

```java
// shared/infrastructure/config/properties/DatabaseProperties.java
@ConfigurationProperties(prefix = "app.database")
public record DatabaseProperties(
        String url,
        String username,
        @DurationUnit(ChronoUnit.SECONDS) Duration connectionTimeout,
        int maxPoolSize
) {}
```

```java
// shared/infrastructure/config/properties/ExternalApiProperties.java
@ConfigurationProperties(prefix = "app.external.payment")
public record ExternalApiProperties(
        String baseUrl,
        String apiKey,
        @DurationUnit(ChronoUnit.MILLIS) Duration readTimeout
) {}
```

```java
// shared/infrastructure/config/InfrastructureConfig.java
@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, ExternalApiProperties.class})
public class InfrastructureConfig { ... }
```

### Profiles

| Profile     | Uso                                              |
|-------------|--------------------------------------------------|
| `local`     | Desarrollo local — H2 o Docker Compose           |
| `test`      | Tests de integración — Testcontainers            |
| `staging`   | Entorno de pre-producción                        |
| `prod`      | Producción — sin valores por defecto en secrets  |

- Los secrets (`apiKey`, passwords, tokens) **nunca** tienen valor por defecto en `application.yml`. Si no están definidos, la app falla al arrancar.
- Usar `spring.config.import` para cargar desde Vault, AWS Secrets Manager o similar en producción.

```yaml
# application.yml — valores seguros por defecto solo para local/test
app:
  external:
    payment:
      base-url: ${PAYMENT_API_URL}          # obligatorio — falla si no está
      api-key: ${PAYMENT_API_KEY}           # obligatorio — nunca hardcodeado
      read-timeout: ${PAYMENT_TIMEOUT:5s}   # con fallback seguro
```

---

## OpenAPI / Swagger: Convenciones de Documentación

### Configuración base en infraestructura

```java
// shared/infrastructure/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("My App API")
                .version("v1")
                .description("API del contexto de {dominio}"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer").bearerFormat("JWT")));
    }
}
```

### Convenciones de anotaciones en Controllers

- `@Tag` a nivel de clase para agrupar endpoints por recurso.
- `@Operation` en cada método con `summary` (una línea) y `description` (opcional, solo si aporta).
- `@ApiResponse` para documentar explícitamente los códigos de error relevantes.
- Los `@Schema` van en los Request/Response DTOs de infraestructura, **nunca en el dominio**.

```java
// order/infrastructure/adapter/input/rest/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gestión del ciclo de vida de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Pedido duplicado (Idempotency-Key ya procesada)")
    })
    public ResponseEntity<OrderResponse> place(...) { ... }
}
```

```java
// order/infrastructure/adapter/input/rest/dto/PlaceOrderRequest.java
public record PlaceOrderRequest(

    @Schema(description = "Lista de líneas del pedido", minLength = 1)
    @NotEmpty List<OrderLineRequest> lines
) {}
```

### Qué documentar y qué no

| Documentar siempre                              | No documentar                              |
|-------------------------------------------------|--------------------------------------------|
| Todos los endpoints públicos                    | Endpoints de Actuator (ya tienen su UI)    |
| Códigos de error de negocio (404, 409, 422)    | Errores genéricos de servidor (500)        |
| Headers obligatorios (`Idempotency-Key`, etc.) | Detalles de implementación interna         |
| Estructura de `ProblemDetail` en errores        | Clases de dominio o Application Services  |

---

## Pipeline CI: Checks Obligatorios antes de Merge

Todo PR debe pasar estos checks en orden. Si uno falla, no se mergea.

```
1. ./gradlew spotlessCheck              ← Formato de código
2. ./gradlew checkstyleMain             ← Reglas de estilo
3. ./gradlew test --tests "..domain..*" ← Tests de dominio (sin Spring, rápidos)
4. ./gradlew test --tests "..application..*" ← Tests de aplicación (Mockito, sin Spring)
5. ./gradlew test --tests "..infrastructure..*" ← Tests de integración (Testcontainers)
6. ./gradlew test --tests "*ArchitectureTest"   ← Tests de arquitectura (ArchUnit)
```

### Reglas del pipeline

- Los pasos 1–4 deben completarse en **menos de 2 minutos**. Si no, hay un problema de diseño (tests unitarios que levantan Spring).
- Testcontainers en paso 5 puede reutilizar el contenedor entre tests (`@Testcontainers` con `reuse = true` en local).
- **El build de `main` nunca puede estar en rojo.** Si se rompe, es prioridad máxima antes que cualquier feature.
- La cobertura no es una métrica de CI — ArchUnit y la pirámide de tests son la garantía real de calidad.

### Ejemplo de workflow GitHub Actions (endurecido)

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

# Cancela runs anteriores del mismo PR — solo en ramas de feature, no en main
# (cancelar un run activo en main podría interrumpir un despliegue)
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}

# Principio de mínimo privilegio: solo lectura por defecto
permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      # Acción oficial de Gradle: gestiona JDK, Gradle wrapper y build cache remoto
      - uses: gradle/actions/setup-gradle@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          gradle-version: '9.4.0'
          # Build cache remoto — acelera builds repetidos en CI
          cache-encryption-key: ${{ secrets.GRADLE_CACHE_ENCRYPTION_KEY }}

      - name: Format check
        run: ./gradlew spotlessCheck

      - name: Style check
        run: ./gradlew checkstyleMain

      - name: Unit tests — domain + application
        run: ./gradlew test --tests "..domain..*" --tests "..application..*"

      - name: Architecture tests
        run: ./gradlew test --tests "*ArchitectureTest"

      - name: Integration & E2E tests
        run: ./gradlew test --tests "..infrastructure..*"
        env:
          TESTCONTAINERS_REUSE_ENABLE: true
          # Secrets de test inyectados desde GitHub Actions Secrets
          TEST_DATABASE_URL: ${{ secrets.TEST_DATABASE_URL }}
          PAYMENT_API_KEY: ${{ secrets.PAYMENT_API_KEY }}

      - name: Upload test results on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: build/reports/tests/
          retention-days: 7
```

---

## Dependabot: actualización automática de dependencias

Dependabot revisa el `libs.versions.toml` y el workflow de GitHub Actions y abre PRs automáticos cuando hay versiones nuevas. Sin él, las dependencias envejecen silenciosamente.

```yaml
# .github/dependabot.yml
version: 2

updates:
  # Dependencias Gradle via Version Catalog
  - package-ecosystem: gradle
    directory: /
    schedule:
      interval: weekly
      day: monday
      time: "08:00"
      timezone: "Europe/Madrid"
    open-pull-requests-limit: 5
    groups:
      # Agrupa todas las dependencias de Spring en un solo PR
      spring:
        patterns: ["org.springframework*", "io.spring*"]
      # Agrupa las herramientas de test
      testing:
        patterns: ["org.junit*", "org.mockito*", "org.testcontainers*", "com.tngtech*"]
    labels:
      - dependencies
      - gradle

  # GitHub Actions
  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
      day: monday
      time: "08:00"
      timezone: "Europe/Madrid"
    labels:
      - dependencies
      - github-actions
```

### Reglas para PRs de Dependabot

- Los PRs de Dependabot deben pasar el pipeline CI completo antes de mergear — igual que cualquier otro PR.
- **Patch y minor** se pueden mergear sin revisión si el CI pasa.
- **Major** requiere revisión manual — puede haber breaking changes.
- Si un PR de Dependabot lleva más de 2 semanas sin mergear, revisar si hay un motivo bloqueante.

---

## N+1 y Fetch Strategy en JPA

### Regla base: todo `LAZY`, `@EntityGraph` explícito cuando se necesite

```java
// ✅ Por defecto: todas las asociaciones en LAZY
@Entity
public class OrderEntity {

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderLineEntity> lines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;  // si se necesita (normalmente solo el ID basta)
}
```

**Nunca** usar `FetchType.EAGER`. Causa N+1 silenciosos y carga datos que nadie necesita.

### Cuándo y cómo usar `@EntityGraph`

Usar `@EntityGraph` en el `Spring*Repository` cuando un caso de uso concreto necesita el Aggregate completo con sus colecciones en una sola query.

```java
// order/infrastructure/adapter/output/persistence/repository/SpringOrderRepository.java
public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    // Carga el aggregate completo para operaciones de escritura que necesitan las líneas
    @EntityGraph(attributePaths = {"lines", "lines.product"})
    Optional<OrderEntity> findWithLinesById(UUID id);

    // Query simple sin colecciones — para reconstitución ligera
    Optional<OrderEntity> findById(UUID id);  // heredado, sin EntityGraph
}
```

```java
// order/infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public Optional<Order> findById(OrderId id) {
    // Elegir el método según lo que el caso de uso necesita
    return repository.findWithLinesById(id.value()).map(mapper::toDomain);
}
```

### Read side: sin `@EntityGraph`, proyecciones directas

Los Query Handlers del lado CQRS nunca cargan Aggregates completos. Usan proyecciones JPQL o Spring Data Projections que traen solo los campos necesarios en una query, eliminando el problema N+1 por diseño.

```java
// order/infrastructure/adapter/output/persistence/repository/SpringOrderRepository.java
@Query("""
    SELECT new com.{ctx}.order.application.dto.OrderSummaryView(
        o.id, o.status, o.createdAt, c.name, SUM(l.unitPrice * l.quantity)
    )
    FROM OrderEntity o
    JOIN CustomerEntity c ON c.id = o.customerId
    JOIN OrderLineEntity l ON l.order = o
    WHERE o.customerId = :customerId
    GROUP BY o.id, o.status, o.createdAt, c.name
    """)
Page<OrderSummaryView> findSummariesByCustomer(@Param("customerId") UUID customerId, Pageable pageable);
```

### Detectar N+1 en desarrollo

Activar en el profile `local`:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
logging:
  level:
    org.hibernate.stat: DEBUG
```

Si una query genera más de una sentencia SQL por registro, hay un N+1. Añadir `@EntityGraph` o reescribir como proyección.

---

## Auditoría: createdAt, updatedAt, createdBy sin contaminar el dominio

Los metadatos de auditoría son un **detalle de persistencia**, no un concepto del dominio. El Aggregate nunca conoce `@CreatedDate`, `@LastModifiedBy` ni `@EntityListeners`.

### Patrón: auditoría solo en la Entity JPA

```java
// order/infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)  // solo aquí
public class OrderEntity {

    @Id
    private UUID id;

    // ... campos de negocio

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;
}
```

```java
// shared/infrastructure/config/PersistenceConfig.java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorProvider(AuthenticatedUserResolver resolver) {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(resolver::resolveUsername);
    }
}
```

### Cuándo la auditoría SÍ pertenece al dominio

Si el negocio necesita razonar sobre quién hizo qué y cuándo (p.ej. auditoría de cumplimiento, trazabilidad legal), entonces es un concepto de dominio explícito y se modela como Domain Event o como un Value Object de auditoría propio:

```java
// shared/domain/model/AuditTrail.java  — solo si el negocio lo requiere explícitamente
public record AuditTrail(UserId performedBy, Instant occurredOn, String action) {}
```

En ese caso, no usar `@CreatedBy` de Spring Data — modelarlo como parte del Aggregate y persistirlo como campo normal.

**Regla:** si el negocio nunca pregunta por esos datos en sus reglas, es auditoría técnica y va solo en la `*Entity`. Si el negocio los necesita para tomar decisiones, es dominio.

---

## Optimistic Locking: concurrencia sin bloqueos

### Principio

`@Version` es un detalle de persistencia JPA. **Nunca** en el Aggregate del dominio. La entidad JPA lleva la versión; el Aggregate la desconoce.

```java
// ✅ Solo en la Entity JPA
@Entity
public class OrderEntity {
    @Version
    private Long version;
    // ...
}

// ✅ El Aggregate no sabe nada de versiones
public class Order {
    // sin @Version, sin campo version
}
```

### Traducir la excepción de JPA a excepción de dominio

Spring lanza `ObjectOptimisticLockingFailureException` cuando hay conflicto. Debe traducirse a una excepción de dominio significativa **en el JPA Adapter**, antes de que suba a la capa de aplicación.

```java
// order/infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public void save(Order order) {
    try {
        var entity = mapper.toEntity(order);
        repository.save(entity);
    } catch (ObjectOptimisticLockingFailureException ex) {
        throw new OrderConcurrentModificationException(order.getId(), ex);
    }
}
```

```java
// order/domain/exception/OrderConcurrentModificationException.java
public class OrderConcurrentModificationException extends ConflictException {
    public OrderConcurrentModificationException(OrderId id, Throwable cause) {
        super("Order modified concurrently: " + id, cause);  // cause se propaga correctamente
    }
}
```

```java
// shared/infrastructure/adapter/input/rest/GlobalExceptionHandler.java
// Capturado por categoría ConflictException → 409, definido en la jerarquía de excepciones
// No hace falta handler específico para OrderConcurrentModificationException
```

### Cuándo aplicar optimistic locking

Aplicar en Aggregates que:
- Son modificados frecuentemente por múltiples usuarios o procesos concurrentes.
- Tienen operaciones de lectura-modificación-escritura (leer, calcular, guardar).

No aplicar en entidades de solo escritura o con baja contención — añade overhead innecesario.

---

## Soft Delete: convención única

### Decisión de diseño: siempre un detalle de persistencia

El borrado lógico **no es un concepto del dominio** en este proyecto. El dominio solo conoce estados de negocio explícitos (`CANCELLED`, `CLOSED`, `ARCHIVED`). La columna `deleted_at` o `active` es un detalle del adaptador de persistencia.

**Consecuencia directa:** los repositorios del dominio nunca devuelven registros "borrados". El filtrado es responsabilidad del JPA Adapter, invisible para el dominio y la aplicación.

### Implementación

```java
// order/infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@SQLRestriction("deleted_at IS NULL")  // filtro automático en todas las queries
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")  // override del DELETE
public class OrderEntity {

    // ... campos de negocio y auditoría

    @Column(name = "deleted_at")
    private Instant deletedAt;  // null = activo, non-null = borrado
}
```

```java
// order/infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public void deleteById(OrderId id) {
    // Spring Data ejecutará el @SQLDelete en lugar del DELETE físico
    repository.deleteById(id.value());
}
```

### Recuperar registros borrados (operaciones de administración)

Si hay casos de uso administrativos que necesitan ver registros borrados, usar una query nativa explícita que ignore el `@SQLRestriction`, **nunca** exponer esto a través del Output Port estándar — crear un Output Port separado para operaciones de administración.

```java
// order/application/port/output/OrderAdminQueryPort.java
public interface OrderAdminQueryPort {
    List<OrderSummaryView> findAllIncludingDeleted(Pageable pageable);
}
```

### Reglas del Soft Delete

- La columna se llama siempre `deleted_at` (timestamp), nunca `is_deleted` (boolean) — el timestamp es más informativo y permite auditoría.
- Usar `@SQLRestriction` de Hibernate 6+ en lugar del deprecado `@Where`.
- Las migraciones Flyway deben añadir índice parcial en producción: `CREATE INDEX ON orders (id) WHERE deleted_at IS NULL`.
- **Nunca** exponer `deletedAt` fuera de la `*Entity` JPA. El Aggregate no tiene ese campo.

---

## Lo que NUNCA se debe hacer

```java
// ❌ Constructor público en Aggregate usado para creación de negocio
new Order(id, customerId, status, lines);   // usar Order.create(customerId, lines)

// ❌ Excepción concreta capturada en GlobalExceptionHandler en lugar de categoría
@ExceptionHandler(OrderNotFoundException.class)   // capturar NotFoundException, no la concreta
@ExceptionHandler(ProductNotFoundException.class) // mismo status, duplicar handlers no escala

// ❌ FetchType.EAGER en cualquier asociación JPA
@OneToMany(fetch = FetchType.EAGER)  // siempre LAZY + @EntityGraph explícito si se necesita

// ❌ Auditoría técnica en el Aggregate del dominio
public class Order {
    @CreatedDate private Instant createdAt;       // detalle de persistencia
    @LastModifiedBy private String updatedBy;     // detalle de persistencia
}

// ❌ @Version en el Aggregate del dominio
public class Order {
    @Version private Long version;   // solo en OrderEntity
}

// ❌ Soft delete como campo del dominio
public class Order {
    private boolean deleted;         // el dominio usa estados de negocio: CANCELLED, CLOSED…
}

// ❌ ObjectOptimisticLockingFailureException llegando al controller sin traducir
// (debe convertirse a excepción de dominio en el JPA Adapter)

// ❌ Spring Security en dominio o aplicación
var auth = SecurityContextHolder.getContext().getAuthentication(); // en dominio o app

// ❌ @Value o Environment fuera de infraestructura
@Value("${app.timeout}") private Duration timeout;  // en dominio o app

// ❌ Sin protección de idempotencia en endpoints que crean recursos
// (POST sin Idempotency-Key cuando hay riesgo de reintento)

// ❌ Dos Aggregates modificados en la misma transacción
repository.save(order);
repository.save(inventory);

// ❌ Anotaciones Spring en dominio o aplicación
@Service public class PlaceOrderHandler { ... }
@Transactional public class PlaceOrderHandler { ... }

// ❌ JPA Entity en el dominio
@Entity public class Order { ... }

// ❌ Spring Data en el dominio
public interface OrderRepository extends JpaRepository<Order, UUID> { ... }

// ❌ El controller llama al Handler directamente (saltarse el puerto)
@Autowired private PlaceOrderHandler handler;   // debe ser PlaceOrderUseCase

// ❌ Versiones hardcodeadas en build.gradle.kts
implementation("org.mapstruct:mapstruct:1.6.0")  // usar libs.mapstruct

// ❌ ddl-auto destructivo fuera de tests
spring.jpa.hibernate.ddl-auto=create-drop        // usar validate o none

// ❌ Devolver null desde consultas
public Order findById(OrderId id) { return null; }  // usar Optional<Order>

// ❌ Logs con concatenación
log.info("Order " + orderId + " placed");            // usar parámetros {}

// ❌ Lógica de negocio en el controller
if (request.amount() <= 0) throw new RuntimeException("invalid"); // va en el dominio

// ❌ Publicar Domain Events antes de persistir
eventPublisher.publish(event);
repository.save(order);  // orden incorrecto: primero save, luego publish

// ❌ Jerga técnica en el dominio
public class OrderProcessor { }
public void processOrderData(OrderDTO dto) { }
```

---

## Respuestas de Claude: Cómo Trabajar Conmigo

- **No explicar** conceptos básicos de Java, Spring, DDD, CQRS o arquitectura hexagonal — soy Senior.
- **Dar código completo y funcional**, nunca fragmentos con `// ...resto del código`.
- Respetar **todas** las convenciones de nomenclatura de este documento en el código generado.
- Antes de sugerir una solución, verificar que encaja en la capa y paquete correctos.
- Si varias soluciones son válidas, presentar las alternativas con trade-offs y recomendar una.
- Los comentarios en código solo cuando explican **el porqué**, nunca el qué.
- Al modificar código existente, no tocar lo que no fue pedido.
- Si una petición viola las reglas arquitectónicas de este documento, **advertirlo** y proponer la alternativa correcta antes de implementar.
- Generar siempre el test unitario correspondiente junto con el código de producción.
- Al generar una clase nueva, indicar en qué paquete exacto debe vivir.
- Cuando generes un endpoint REST, incluir siempre: anotaciones OpenAPI, extracción de identidad del `Authentication`, y el header `Idempotency-Key` si el endpoint crea o modifica recursos.
- Cuando generes un Command Handler, evaluar siempre si necesita protección de idempotencia.
- Nunca resolver `SecurityContextHolder` fuera de `infrastructure/`.
