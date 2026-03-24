# 02 — DDD: Modelo de Dominio

> [← Índice](../../CLAUDE.md)

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

- **`create()` estático** — punto de entrada de negocio. Aplica invariantes, genera el ID, emite el Domain Event inicial.
- **Constructor de paquete/privado** — exclusivo para el mapper de persistencia. Reconstituye el Aggregate sin disparar lógica ni eventos.

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

### Cuándo usar un Builder

Solo si el Aggregate tiene **más de 4-5 parámetros opcionales**. El Builder es estático y anidado en la clase del Aggregate.

```java
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

- **Síncrona (dentro de la transacción):** Output Port `DomainEventPublisherPort`.
- **Outbox Pattern** para eventos críticos: persistir el evento en la misma transacción que el aggregate, publicar asíncronamente con poller o CDC.
- **Nunca** publicar eventos antes de confirmar la persistencia del aggregate.

---

## Diseño de Aggregates: Reglas de Oro

### Regla 1: Referenciar otros Aggregates solo por ID

```java
// ❌ Referencia directa a otro Aggregate
public class Order {
    private Customer customer;
}

// ✅ Referencia por ID
public class Order {
    private final CustomerId customerId;
}
```

### Regla 2: Mantener los Aggregates pequeños

Señales de que un Aggregate es demasiado grande:
- Las transacciones tardan más de lo esperado.
- Los tests requieren construir objetos muy complejos.
- Múltiples casos de uso distintos modifican siempre el mismo Aggregate.

### Regla 3: Un Aggregate por transacción

```java
// ❌ Dos Aggregates en la misma transacción
repository.save(order);
repository.save(inventory);

// ✅ Order emite evento, Inventory reacciona de forma eventual
order.confirm();
repository.save(order);
order.pullDomainEvents().forEach(eventPublisher::publish);
// InventoryReservationHandler escucha OrderConfirmed en su propia transacción
```

### Regla 4: Los invariantes se protegen siempre en el Aggregate Root

```java
// ❌ Handler manipula estado interno directamente
order.getLines().add(new OrderLine(...));

// ✅ El Aggregate Root controla sus invariantes
order.addLine(productId, quantity, unitPrice);
```

