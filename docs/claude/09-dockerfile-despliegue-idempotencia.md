# 09 — Dockerfile, Despliegue e Idempotencia

> [← Índice](../../CLAUDE.md)

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

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### Reglas del Dockerfile

- **Siempre multi-stage**: `builder` con JDK completo, `runtime` solo con JRE.
- **Non-root user** (`appuser`) — obligatorio. Nunca `USER root` en runtime.
- **`eclipse-temurin:25-jre-alpine`** como base del runtime — imagen mínima.
- `-XX:+UseContainerSupport` — la JVM respeta los límites de memoria del contenedor.
- `-XX:MaxRAMPercentage=75.0` — la JVM usa máximo el 75% de la RAM asignada.
- Nunca hardcodear variables de entorno en el `Dockerfile`.

---

## Estrategia de Despliegue

### Health check y verificación post-deploy

```yaml
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

```yaml
# docker-compose.yml (producción simplificada)
services:
  app:
    image: ghcr.io/myorg/myapp:${APP_VERSION}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 30s
        failure_action: rollback
        order: start-first      # arrancar nueva antes de parar la vieja
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

### Rollback

```bash
# Tag de rollback semántico
git tag -a v1.1.1-hotfix -m "revert: rollback to v1.1.0 due to issue #XXX"
git push origin v1.1.1-hotfix

# O directamente con docker
docker service update --image ghcr.io/myorg/myapp:v1.1.0 myapp_app
```

### Reglas de despliegue

- **Nunca desplegar en viernes** salvo hotfix urgente.
- Toda nueva versión pasa primero por `staging`.
- Las migraciones Flyway se ejecutan automáticamente al arrancar — si fallan, la app no arranca.
- Si una migración puede ser destructiva, dividirla en dos releases: primero deploy sin la migración destructiva, luego deploy con ella.

---

## Idempotencia

Toda operación que pueda ser reintentada debe ser idempotente o estar protegida contra duplicados.

### Idempotencia en Commands de API REST

```java
// order/infrastructure/adapter/input/rest/OrderController.java
@PostMapping
public ResponseEntity<OrderResponse> place(
        @Valid @RequestBody PlaceOrderRequest request,
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        Authentication authentication) {
    var customerId = userResolver.resolveCustomerId(authentication);
    var command = new PlaceOrderCommand(customerId, request.lines(), idempotencyKey);
    // ...
}
```

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

**Regla:** el `messageId` debe persistirse **en la misma transacción** que el efecto de negocio.

