# 07 — Testing y Nomenclatura

> [← Índice](../../CLAUDE.md)

---

## Nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Entidad / Aggregate Root | Sustantivo singular del dominio | `Order`, `Product` |
| Value Object | Sustantivo descriptivo del concepto | `Money`, `TrackingNumber` |
| Domain Event | Sustantivo + participio pasado | `OrderPlaced`, `OrderShipped` |
| Domain Service | Sustantivo + "Service" | `PricingService` |
| Domain Exception | Sustantivo + "Exception" | `InsufficientStockException` |
| Command | Verbo imperativo + "Command" | `PlaceOrderCommand` |
| Query | "Find/Get/List" + sustantivo + "Query" | `FindOrderByIdQuery` |
| Command Use Case (port) | Verbo imperativo + "UseCase" | `PlaceOrderUseCase` |
| Query Use Case (port) | "Get/List/Find" + sustantivo + "UseCase" | `GetOrderByIdUseCase` |
| Command Handler | Verbo + "Handler" | `PlaceOrderHandler` |
| Query Handler | "Get/List/Find" + sustantivo + "Handler" | `GetOrderByIdHandler` |
| Response / View | Sustantivo + "Response" / "View" | `OrderDetailView` |
| Output Port (repo) | Sustantivo + "RepositoryPort" | `OrderRepositoryPort` |
| Output Port (query) | Sustantivo + "QueryPort" | `OrderQueryPort` |
| Output Port (events) | Sustantivo + "PublisherPort" | `DomainEventPublisherPort` |
| JPA Entity | Sustantivo + "Entity" | `OrderEntity` |
| Spring Data Repository | "Spring" + Sustantivo + "Repository" | `SpringOrderRepository` |
| JPA Adapter | Sustantivo + "JpaAdapter" | `OrderJpaAdapter` |
| REST Controller | Sustantivo + "Controller" | `OrderController` |
| Mapper de persistencia | Sustantivo + "PersistenceMapper" | `OrderPersistenceMapper` |
| @Configuration de beans | Sustantivo + "Config" | `OrderConfig` |

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
        order.cancel(UserId.of(UUID.randomUUID()), CancellationReason.CUSTOMER_REQUEST);
        return order;
    }

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
// shared/domain/CustomerIdMother.java
public final class CustomerIdMother {
    public static CustomerId any() { return CustomerId.of(UUID.randomUUID()); }
    public static CustomerId fixed() { return CustomerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")); }
}

public final class TrackingNumberMother {
    public static TrackingNumber any() {
        return new TrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}

public final class OrderLineMother {
    private static final Currency EUR = Currency.getInstance("EUR");
    public static OrderLineCommand withProduct(ProductId productId, int quantity) {
        return new OrderLineCommand(productId, quantity, new Money(new BigDecimal("9.99"), EUR));
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
- Los métodos describen **estados de negocio**.
- Los `*Mother` de Value Objects compartidos van en `shared/`.
- Los `*Mother` de Aggregates van en la carpeta de test de su feature.
- **Nunca** usar `*Mother` en código de producción.

---

## Calidad de Código: Tooling

### Spotless

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

- `./gradlew spotlessCheck` en CI.
- `./gradlew spotlessApply` para formatear localmente antes de commit.

### Checkstyle

Reglas mínimas:
- Sin imports con `*`.
- Javadoc obligatorio en interfaces públicas de puertos.
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

    @ArchTest
    static final ArchRule orderDoesNotImportProductDomain =
        noClasses().that().resideInAPackage("..order..")
                   .should().dependOnClassesThat()
                   .resideInAPackage("..product..domain..");
}
```

