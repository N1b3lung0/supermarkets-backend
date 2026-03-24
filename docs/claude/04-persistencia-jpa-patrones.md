# 04 — Persistencia: Flyway, JPA y Patrones

> [← Índice](../../CLAUDE.md)

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
    private CustomerEntity customer;
}
```

**Nunca** usar `FetchType.EAGER`. Causa N+1 silenciosos y carga datos innecesarios.

### Cuándo y cómo usar `@EntityGraph`

```java
// order/infrastructure/adapter/output/persistence/repository/SpringOrderRepository.java
public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = {"lines", "lines.product"})
    Optional<OrderEntity> findWithLinesById(UUID id);

    Optional<OrderEntity> findById(UUID id);  // sin EntityGraph
}
```

```java
// order/infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public Optional<Order> findById(OrderId id) {
    return repository.findWithLinesById(id.value()).map(mapper::toDomain);
}
```

### Read side: proyecciones directas, sin EntityGraph

```java
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

---

## Auditoría: createdAt, updatedAt, createdBy sin contaminar el dominio

Los metadatos de auditoría son un **detalle de persistencia**. El Aggregate nunca conoce `@CreatedDate`, `@LastModifiedBy` ni `@EntityListeners`.

### Patrón: auditoría solo en la Entity JPA

```java
// order/infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)  // solo aquí
public class OrderEntity {

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

Si el negocio necesita razonar sobre quién hizo qué (auditoría de cumplimiento, trazabilidad legal), se modela como Domain Event o como un Value Object propio:

```java
// shared/domain/model/AuditTrail.java — solo si el negocio lo requiere explícitamente
public record AuditTrail(UserId performedBy, Instant occurredOn, String action) {}
```

**Regla:** si el negocio nunca pregunta por esos datos en sus reglas → auditoría técnica en `*Entity`. Si los necesita para tomar decisiones → dominio.

---

## Optimistic Locking: concurrencia sin bloqueos

`@Version` es un detalle de persistencia JPA. **Nunca** en el Aggregate del dominio.

```java
// ✅ Solo en la Entity JPA
@Entity
public class OrderEntity {
    @Version
    private Long version;
}

// ✅ El Aggregate no sabe nada de versiones
public class Order {
    // sin @Version, sin campo version
}
```

### Traducir la excepción de JPA a excepción de dominio

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

El `GlobalExceptionHandler` lo captura por categoría `ConflictException → 409`. No hace falta handler específico.

### Cuándo aplicar optimistic locking

Aplicar en Aggregates con alta contención (múltiples usuarios/procesos modifican el mismo registro). No aplicar en entidades de solo escritura — añade overhead innecesario.

---

## Soft Delete: convención única

El borrado lógico **no es un concepto del dominio**. El dominio solo conoce estados de negocio explícitos (`CANCELLED`, `CLOSED`). La columna `deleted_at` es un detalle del adaptador de persistencia.

### Implementación

```java
// order/infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@SQLRestriction("deleted_at IS NULL")  // filtro automático en todas las queries
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")
public class OrderEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;  // null = activo, non-null = borrado
}
```

```java
// order/infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public void deleteById(OrderId id) {
    repository.deleteById(id.value());  // ejecuta el @SQLDelete
}
```

### Recuperar registros borrados (administración)

Crear un Output Port separado para operaciones de administración — nunca exponer a través del Output Port estándar.

```java
// order/application/port/output/OrderAdminQueryPort.java
public interface OrderAdminQueryPort {
    List<OrderSummaryView> findAllIncludingDeleted(Pageable pageable);
}
```

### Reglas del Soft Delete

- Columna: siempre `deleted_at` (timestamp), nunca `is_deleted` (boolean).
- Usar `@SQLRestriction` de Hibernate 6+ en lugar del deprecado `@Where`.
- Índice parcial en Flyway: `CREATE INDEX ON orders (id) WHERE deleted_at IS NULL`.
- **Nunca** exponer `deletedAt` fuera de la `*Entity`. El Aggregate no tiene ese campo.

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

