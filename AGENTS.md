# AGENTS.md

AI coding agent reference for the **Supermarkets Price Comparator** — Java 25 + Spring Boot 4, Hexagonal Architecture, DDD, CQRS.

---

## Architecture Overview

**Feature-first hexagonal layout.** Every bounded context is a self-contained vertical under `com.n1b3lung0.supermarkets.<feature>/`:

```
{feature}/
  domain/         ← Pure Java: Aggregates, Value Objects, Domain Events, Exceptions
  application/    ← Use Cases (ports), Command/Query Handlers, DTOs
  infrastructure/ ← REST Controllers, JPA Adapters, Spring Config, Scrapers
```

**Bounded contexts (main):** `supermarket` · `category` · `product` · `basket` · `comparison` · `sync` · `shared`

**Scraper adapters (infrastructure-only, no domain):** `alcampo` · `aldi` · `carrefour` · `dia` · `lidl` · `mercadona`

**Dependency direction (strict, enforced by ArchUnit):**
```
infrastructure → application → domain   (never reversed)
```

Cross-feature communication: reference only by ID Value Object, coordinate via Domain Events. See `SyncSupermarketCatalogHandler` for cross-feature orchestration via ports.

---

## Critical Conventions

### Domain & Application: zero Spring annotations
No `@Service`, `@Component`, `@Transactional`, `@Value` in `domain/` or `application/`. All wiring is manual in `{Feature}Config.java`:
```java
// supermarket/infrastructure/config/SupermarketConfig.java
@Bean
public RegisterSupermarketUseCase registerSupermarketUseCase(SupermarketJpaAdapter adapter) {
    return new RegisterSupermarketHandler(adapter);  // plain constructor — no @Service
}
```

### Aggregate factory methods
- `Aggregate.create(...)` — business entry point; validates invariants, emits Domain Event.
- `Aggregate.reconstitute(...)` — persistence mapper only; no events, no re-validation.

See `Supermarket.java` for the canonical example.

### Controllers inject UseCase ports, not Handlers
```java
@Autowired private PlaceOrderUseCase placeOrderUseCase;  // ✅ port interface
@Autowired private PlaceOrderHandler handler;             // ❌ never
```

### Cross-cutting concerns are infrastructure decorators
Caching → `CachingGetProductByIdUseCase`, metrics → `MeteredRecordProductPriceUseCase`.
Transactions → `TransactionalXxxUseCase` wrapping the handler via `TransactionTemplate`.
None of these patterns use `@Transactional` on application classes.

### JPA rules
- All associations `FetchType.LAZY`. Use `@EntityGraph` only when a specific use case requires it.
- `@Version`, `@CreatedDate`, `@SQLRestriction` live only on `*Entity`, never on domain classes.
- Soft delete: `deleted_at` timestamp + `@SQLRestriction("deleted_at IS NULL")` + `@SQLDelete`.

### Exception hierarchy → HTTP mapping (GlobalExceptionHandler catches by category)
```
NotFoundException          → 404   BusinessRuleViolationException → 422
ConflictException          → 409   UnauthorizedException          → 403
ExternalServiceException   → 502
```
Never add a handler for a concrete exception like `OrderNotFoundException`.

### All create/mutate endpoints require `Idempotency-Key` header.

---

## Database Specifics

- `product_prices` is a **time-partitioned table** (RANGE by `recorded_at`). New month partitions are created dynamically by `PartitionMaintenancePort` before each sync.
- `latest_product_prices` is a **materialized view** refreshed `CONCURRENTLY` after every sync via `LatestPricesRefreshPort.refresh()`. Comparison queries read from this view.
- Flyway migrations in `src/main/resources/db/migration/`. Naming: `V{n}__{description}.sql`. Never modify applied migrations.

---

## Developer Workflows

```bash
# Start PostgreSQL + Redis
docker compose up -d

# Run app
./gradlew bootRun

# Format (always before committing)
./gradlew spotlessApply

# Full local quality gate
./gradlew spotlessCheck checkstyleMain test

# Architecture boundary tests only
./gradlew test --tests "*ArchitectureTest"

# OWASP CVE scan (slow — not in default check)
./gradlew dependencyCheckAnalyze
```

Install pre-commit hook (runs Spotless + Checkstyle on every commit):
```bash
chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh
```

All dependency versions live exclusively in `gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts`.

---

## Testing Conventions

- Domain tests: **no mocks** — pure Java, no Spring context.
- Application handler tests: mock Output Ports with Mockito.
- REST layer: `@WebMvcTest` only.
- JPA adapters: `@DataJpaTest` + Testcontainers.
- Test fixtures: `*Mother` classes (e.g. `OrderMother.pending()`, `OrderMother.shipped()`). Method names describe **business state**, not data shape.
- Test naming: `should{Outcome}_when{Condition}()` with Given/When/Then comments.

---

## Key Files

| File | Purpose |
|---|---|
| `docs/claude/` | Full architecture reference (10 docs) |
| `shared/infrastructure/config/SecurityConfig.java` | Stateless JWT / OAuth2 resource server |
| `shared/infrastructure/adapter/input/rest/GlobalExceptionHandler.java` | Central error mapping |
| `sync/application/command/SyncSupermarketCatalogHandler.java` | Cross-feature sync orchestration |
| `sync/infrastructure/adapter/input/scheduler/DailySyncScheduler.java` | ShedLock-protected daily job (03:00 Europe/Madrid) |
| `gradle/libs.versions.toml` | All dependency versions |
| `src/main/resources/db/migration/` | Flyway migrations |

