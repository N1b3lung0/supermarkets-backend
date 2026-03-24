# 05 — API REST, Observabilidad y Configuración

> [← Índice](../../CLAUDE.md)

---

## Convenciones de API REST

### Problem Details (RFC 9457)

Todos los errores devuelven `application/problem+json`. Usar `ProblemDetail` de Spring 6+. El `GlobalExceptionHandler` captura siempre por **categoría base**, nunca por excepción concreta.

### Versionado de API

- Versionar en la URL: `/api/v1/orders`, `/api/v2/orders`.
- Una versión por `@RequestMapping` de clase en el controller.
- Nunca versionar por header o query param.
- Mantener la versión anterior al menos un ciclo de release tras deprecarla.

### Paginación

Nunca devolver un array crudo para colecciones paginadas:

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

## OpenAPI / Swagger: Convenciones de Documentación

### Configuración base

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

### Anotaciones en Controllers

- `@Tag` a nivel de clase para agrupar endpoints.
- `@Operation` con `summary` en cada método.
- `@ApiResponse` para documentar códigos de error relevantes.
- `@Schema` en los Request/Response DTOs de infraestructura, **nunca en el dominio**.

```java
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
public record PlaceOrderRequest(
    @Schema(description = "Lista de líneas del pedido", minLength = 1)
    @NotEmpty List<OrderLineRequest> lines
) {}
```

### Qué documentar y qué no

| Documentar siempre | No documentar |
|---|---|
| Todos los endpoints públicos | Endpoints de Actuator |
| Códigos de error de negocio (404, 409, 422) | Errores genéricos de servidor (500) |
| Headers obligatorios (`Idempotency-Key`) | Detalles de implementación interna |
| Estructura de `ProblemDetail` en errores | Clases de dominio o Application Services |

---

## Observabilidad

### Structured Logging

- **Nunca concatenar strings en logs.** Usar siempre parámetros con `{}`.
- Logs en **formato JSON en producción** (Logback + `logstash-logback-encoder`).
- Incluir siempre `traceId` y `spanId` (propagados automáticamente por Micrometer Tracing vía MDC).
- Nivel por defecto `INFO`. `DEBUG` solo en desarrollo.

```java
// ✅
log.info("Order placed orderId={} customerId={}", order.getId(), command.customerId());

// ❌
log.info("Order " + orderId + " placed for customer " + customerId);
```

### Métricas con Micrometer

- Registrar métricas de negocio en los Command/Query Handlers.
- `MeterRegistry` inyectado como dependencia desde config.
- Nombrar en snake_case con prefijo de contexto: `orders.placed.total`, `orders.cancelled.total`.

```java
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

## Configuración Externalizada (12-Factor)

### `@ConfigurationProperties` solo en infraestructura

Nunca `@Value` o `Environment` en dominio ni aplicación.

```java
// shared/infrastructure/config/properties/DatabaseProperties.java
@ConfigurationProperties(prefix = "app.database")
public record DatabaseProperties(
        String url,
        String username,
        @DurationUnit(ChronoUnit.SECONDS) Duration connectionTimeout,
        int maxPoolSize
) {}

@ConfigurationProperties(prefix = "app.external.payment")
public record ExternalApiProperties(
        String baseUrl,
        String apiKey,
        @DurationUnit(ChronoUnit.MILLIS) Duration readTimeout
) {}
```

### Profiles

| Profile  | Uso |
|---|---|
| `local`   | Desarrollo local — H2 o Docker Compose |
| `test`    | Tests de integración — Testcontainers |
| `staging` | Entorno de pre-producción |
| `prod`    | Producción — sin valores por defecto en secrets |

Los secrets **nunca** tienen valor por defecto. Si no están definidos, la app falla al arrancar.

```yaml
app:
  external:
    payment:
      base-url: ${PAYMENT_API_URL}          # obligatorio — falla si no está
      api-key: ${PAYMENT_API_KEY}           # obligatorio — nunca hardcodeado
      read-timeout: ${PAYMENT_TIMEOUT:5s}   # con fallback seguro
```

