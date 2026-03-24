# 10 — Lo que NUNCA se debe hacer y Respuestas de Claude

> [← Índice](../../CLAUDE.md)

---

## Lo que NUNCA se debe hacer

```java
// ❌ Constructor público en Aggregate para creación de negocio
new Order(id, customerId, status, lines);   // usar Order.create(customerId, lines)

// ❌ Excepción concreta capturada en GlobalExceptionHandler
@ExceptionHandler(OrderNotFoundException.class)   // capturar NotFoundException, no la concreta
@ExceptionHandler(ProductNotFoundException.class) // mismo status — duplicar no escala

// ❌ FetchType.EAGER en cualquier asociación JPA
@OneToMany(fetch = FetchType.EAGER)  // siempre LAZY + @EntityGraph explícito

// ❌ Auditoría técnica en el Aggregate del dominio
public class Order {
    @CreatedDate private Instant createdAt;
    @LastModifiedBy private String updatedBy;
}

// ❌ @Version en el Aggregate del dominio
public class Order {
    @Version private Long version;   // solo en OrderEntity
}

// ❌ Soft delete como campo del dominio
public class Order {
    private boolean deleted;   // el dominio usa estados: CANCELLED, CLOSED…
}

// ❌ ObjectOptimisticLockingFailureException llegando al controller sin traducir

// ❌ Spring Security en dominio o aplicación
var auth = SecurityContextHolder.getContext().getAuthentication();

// ❌ @Value o Environment fuera de infraestructura
@Value("${app.timeout}") private Duration timeout;  // en dominio o app

// ❌ Sin protección de idempotencia en endpoints que crean recursos

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
spring.jpa.hibernate.ddl-auto=create-drop

// ❌ Devolver null desde consultas
public Order findById(OrderId id) { return null; }  // usar Optional<Order>

// ❌ Logs con concatenación
log.info("Order " + orderId + " placed");   // usar parámetros {}

// ❌ Lógica de negocio en el controller
if (request.amount() <= 0) throw new RuntimeException("invalid");  // va en el dominio

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
- Si una petición viola las reglas arquitectónicas, **advertirlo** y proponer la alternativa correcta antes de implementar.
- Generar siempre el test unitario correspondiente junto con el código de producción.
- Al generar una clase nueva, indicar en qué paquete exacto debe vivir.
- Cuando generes un endpoint REST, incluir siempre: anotaciones OpenAPI, extracción de identidad del `Authentication`, y el header `Idempotency-Key` si el endpoint crea o modifica recursos.
- Cuando generes un Command Handler, evaluar siempre si necesita protección de idempotencia.
- Nunca resolver `SecurityContextHolder` fuera de `infrastructure/`.

