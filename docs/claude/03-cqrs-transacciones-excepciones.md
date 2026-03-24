# 03 — CQRS, Transacciones y Excepciones

> [← Índice](../../CLAUDE.md)

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

### Implementación base

```java
// shared/domain/exception/DomainException.java
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}

public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message) { super(message); }
}

public abstract class BusinessRuleViolationException extends DomainException {
    protected BusinessRuleViolationException(String message) { super(message); }
}

public abstract class ConflictException extends DomainException {
    protected ConflictException(String message) { super(message); }
    protected ConflictException(String message, Throwable cause) { super(message, cause); }
}

public abstract class UnauthorizedException extends DomainException {
    protected UnauthorizedException(String message) { super(message); }
}
```

### Excepciones concretas extienden la categoría correcta

```java
// order/domain/exception/OrderNotFoundException.java
public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}

public class InsufficientStockException extends BusinessRuleViolationException {
    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d available=%d"
            .formatted(productId, requested, available));
    }
}

public class OrderConcurrentModificationException extends ConflictException {
    public OrderConcurrentModificationException(OrderId id, Throwable cause) {
        super("Order modified concurrently: " + id, cause);
    }
}

public class UnauthorizedCancellationException extends UnauthorizedException {
    public UnauthorizedCancellationException(UserId requestedBy, OrderId orderId) {
        super("User %s is not authorized to cancel order %s".formatted(requestedBy, orderId));
    }
}
```

### GlobalExceptionHandler — captura por categoría base, nunca por concreta

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

