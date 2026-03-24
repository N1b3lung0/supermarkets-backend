# Phase 0 — Project Foundation

> **Steps 1–10** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 1 ⬜ — Migrate build to Kotlin DSL + Version Catalog
- Convert `build.gradle` (Groovy) → `build.gradle.kts` (Kotlin DSL)
- Create `gradle/libs.versions.toml` with all current deps + ArchUnit + Testcontainers + Spotless + Checkstyle
- Keep same functionality, zero behaviour change
- **Verify:** `./gradlew build` passes

### Step 2 ⬜ — Add Spotless + Checkstyle configuration
- Configure Spotless with `googleJavaFormat` in `build.gradle.kts`
- Add `config/checkstyle/checkstyle.xml` with project rules (no star imports, 120-char line limit, no empty catch)
- **Verify:** `./gradlew spotlessCheck checkstyleMain` passes on the existing skeleton

### Step 3 ⬜ — Configure PostgreSQL via Docker Compose
- Update `compose.yaml` with a PostgreSQL 17 service
- Add DB config to `application.yaml` (url, username, password via env vars)
- Create `.env.example` with all required environment variables
- Add `.env` to `.gitignore`
- **Verify:** `docker compose up -d` starts Postgres; app connects on startup

### Step 4 ⬜ — Add Flyway + first migration
- Add `flyway-core` and `flyway-database-postgresql` to `libs.versions.toml`
- Set `ddl-auto: validate` and Flyway config in `application.yaml`
- Create `db/migration/V1__init.sql` as a no-op baseline migration
- **Verify:** `./gradlew bootRun` starts without Flyway errors

### Step 5 ⬜ — Create shared domain exception hierarchy
- Create `shared/domain/exception/DomainException.java`
- Create `shared/domain/exception/NotFoundException.java`
- Create `shared/domain/exception/BusinessRuleViolationException.java`
- Create `shared/domain/exception/ConflictException.java`
- Create `shared/domain/exception/UnauthorizedException.java`
- Create `shared/infrastructure/exception/InfrastructureException.java`
- Create `shared/infrastructure/exception/ExternalServiceException.java`
- **Verify:** unit tests asserting hierarchy (extends correct parent)

### Step 6 ⬜ — Create GlobalExceptionHandler + PageResponse
- Create `shared/infrastructure/adapter/input/rest/GlobalExceptionHandler.java` mapping categories to HTTP status using `ProblemDetail`
- Create `shared/domain/model/PageResponse.java` record
- **Verify:** `@WebMvcTest` on a dummy controller verifying 404 → `ProblemDetail` with correct status

### Step 7 ⬜ — Create ArchUnit architecture tests skeleton
- Create `src/test/java/.../ArchitectureTest.java` with rules:
  - Domain has no Spring/JPA dependencies
  - Application has no Infrastructure dependencies
  - No Spring annotations in domain or application
- **Verify:** `./gradlew test --tests "*ArchitectureTest"` passes on the current skeleton

### Step 8 ⬜ — Configure OpenAPI / Swagger
- Add `springdoc-openapi` to `libs.versions.toml`
- Create `shared/infrastructure/config/OpenApiConfig.java`
- **Verify:** `http://localhost:8080/swagger-ui.html` renders

### Step 9 ⬜ — Configure Observability (Actuator + Micrometer)
- Add `micrometer-registry-otlp` and `micrometer-tracing-bridge-otel` to `libs.versions.toml`
- Configure Actuator endpoints in `application.yaml`
- Add Logback JSON config (`logback-spring.xml`) using `logstash-logback-encoder`
- **Verify:** `http://localhost:8080/actuator/health` returns `{"status":"UP"}`

### Step 10 ⬜ — Add pre-commit hooks script
- Create `scripts/setup-hooks.sh` (spotless + checkstyle before each commit)
- Document in `README.md`
- **Verify:** `chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh` installs the hook

