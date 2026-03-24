# 06 — Seguridad

> [← Índice](../../CLAUDE.md)

---

## Seguridad: Spring Security sin contaminar el dominio

El dominio y la aplicación **nunca conocen a Spring Security**. El contexto de seguridad es un detalle de infraestructura.

### Principio: el dominio recibe identidad, no contexto de seguridad

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

```java
// order/infrastructure/adapter/input/rest/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final AuthenticatedUserResolver userResolver;

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request,
                                               Authentication authentication) {
        var customerId = userResolver.resolveCustomerId(authentication);  // conversión aquí
        var command = new PlaceOrderCommand(customerId, request.lines(), null);
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

- `@PreAuthorize` o `SecurityFilterChain` en infraestructura para autorización basada en roles.
- Autorización basada en **reglas de negocio** sí puede vivir en el dominio, pero recibiendo `UserId` como parámetro.

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

Toda la configuración en `shared/infrastructure/config/SecurityConfig.java`. Los beans de Spring Security **nunca** se inyectan fuera de `infrastructure/`.

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

## Secrets en CI y entorno local

### Reglas generales

- **Nunca** commitear un secret.
- El repositorio incluye `.env.example` con claves pero sin valores reales.
- `.env` está en `.gitignore` desde el commit inicial.

```bash
# .env.example  — commitear esto
DATABASE_URL=jdbc:postgresql://localhost:5432/myapp
DATABASE_USERNAME=
DATABASE_PASSWORD=
PAYMENT_API_URL=https://sandbox.payment-provider.com
PAYMENT_API_KEY=
JWT_SECRET=
```

### Secrets en GitHub Actions

```yaml
- name: Integration tests
  run: ./gradlew test --tests "..infrastructure..*"
  env:
    DATABASE_URL: ${{ secrets.TEST_DATABASE_URL }}
    PAYMENT_API_KEY: ${{ secrets.PAYMENT_API_KEY }}
```

### Nunca loguear secrets

```java
// ❌
log.info("Connecting to payment API with key={}", apiKey);

// ✅
log.info("Connecting to payment API baseUrl={}", properties.baseUrl());
```

### Tabla de secrets por entorno

| Tipo | Dónde viven | Ejemplo |
|---|---|---|
| Runtime (producción) | Vault / AWS Secrets Manager / env vars | `DATABASE_PASSWORD`, `JWT_SECRET` |
| CI (build/test) | GitHub Actions Secrets | `TEST_DATABASE_URL`, credenciales de registry |
| Locales (desarrollo) | `.env` (en `.gitignore`) | Cualquiera de los anteriores en local |

---

## Seguridad: Scanning en CI

### OWASP Dependency Check

```toml
# libs.versions.toml
owasp-dependency-check = { id = "org.owasp.dependencycheck", version = "9.x" }
```

```kotlin
// build.gradle.kts
dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFile = "config/owasp-suppressions.xml"
    format = "HTML"
    outputDirectory = "build/reports/dependency-check"
}
```

```yaml
- name: OWASP Dependency Check
  run: ./gradlew dependencyCheckAnalyze

- name: Upload OWASP report
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: owasp-report
    path: build/reports/dependency-check/
    retention-days: 30
```

### Trivy: escaneo de imagen Docker

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
    exit-code: '1'

- name: Upload Trivy results to GitHub Security
  if: always()
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: trivy-results.sarif
```

