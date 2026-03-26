# Phase 1 — Supermarket Bounded Context

> **Steps 11–22** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 11 ✅ — Define Supermarket domain model
- Create `supermarket/domain/model/SupermarketId.java` (record, UUID-based Value Object)
- Create `supermarket/domain/model/SupermarketName.java` (record, non-blank validation)
- Create `supermarket/domain/model/SupermarketCountry.java` (record, ISO country code)
- Create `supermarket/domain/model/Supermarket.java` (Aggregate Root with `create()` factory)
- **Verify:** unit tests on `Supermarket.create()` — happy path + null/blank invariants

### Step 12 ✅ — Define Supermarket domain events
- Create `supermarket/domain/event/SupermarketEvent.java` (sealed interface)
- Create `supermarket/domain/event/SupermarketRegistered.java` (record)
- Add `pullDomainEvents()` to `Supermarket` aggregate
- **Verify:** unit test asserting `Supermarket.create()` emits `SupermarketRegistered`

### Step 13 ✅ — Define Supermarket domain exceptions
- Create `supermarket/domain/exception/SupermarketNotFoundException.java`
- Create `supermarket/domain/exception/DuplicateSupermarketException.java`
- **Verify:** unit tests asserting correct parent class and message format

### Step 14 ✅ — Define Supermarket output ports
- Create `supermarket/application/port/output/SupermarketRepositoryPort.java`
- Create `supermarket/application/port/output/SupermarketQueryPort.java`
- Create `supermarket/application/dto/SupermarketDetailView.java` (record)
- Create `supermarket/application/dto/SupermarketSummaryView.java` (record)
- **Verify:** compile only (interfaces + records)

### Step 15 ✅ — Register Supermarket use case (command)
- Create `supermarket/application/port/input/command/RegisterSupermarketUseCase.java`
- Create `supermarket/application/dto/RegisterSupermarketCommand.java` (record)
- Create `supermarket/application/command/RegisterSupermarketHandler.java`
- **Verify:** unit test with mocked `SupermarketRepositoryPort` — happy path + duplicate check

### Step 16 ✅ — Get Supermarket by ID use case (query)
- Create `supermarket/application/port/input/query/GetSupermarketByIdUseCase.java`
- Create `supermarket/application/dto/GetSupermarketByIdQuery.java` (record)
- Create `supermarket/application/query/GetSupermarketByIdHandler.java`
- **Verify:** unit test with mocked `SupermarketQueryPort` — found + not found

### Step 17 ✅ — List Supermarkets use case (query)
- Create `supermarket/application/port/input/query/ListSupermarketsUseCase.java`
- Create `supermarket/application/dto/ListSupermarketsQuery.java` (record with `Pageable`)
- Create `supermarket/application/query/ListSupermarketsHandler.java`
- **Verify:** unit test returning paginated `PageResponse<SupermarketSummaryView>`

### Step 18 ✅ — Supermarket JPA Entity + Flyway migration
- Create `V2__create_supermarkets_table.sql`
- Create `supermarket/infrastructure/adapter/output/persistence/entity/SupermarketEntity.java`
- Add `@SQLRestriction("deleted_at IS NULL")` + `@SQLDelete` for soft delete
- Add `@EntityListeners(AuditingEntityListener.class)` for audit fields
- **Verify:** Flyway migration runs; JPA validates schema

### Step 19 ✅ — Supermarket persistence mapper + Spring repository
- Create `supermarket/infrastructure/adapter/output/persistence/repository/SpringSupermarketRepository.java`
- Create `supermarket/infrastructure/adapter/output/persistence/mapper/SupermarketPersistenceMapper.java`
- Create `supermarket/infrastructure/adapter/output/persistence/SupermarketJpaAdapter.java` (implements both output ports)
- **Verify:** `@DataJpaTest` + Testcontainers verifying save/find/soft-delete

### Step 20 ✅ — Supermarket Config (bean registration)
- Create `supermarket/infrastructure/config/SupermarketConfig.java` — register all use cases as beans, wrapping handlers with `TransactionTemplate`
- Enable JPA Auditing in `shared/infrastructure/config/PersistenceConfig.java`
- **Verify:** `./gradlew bootRun` starts; beans wire correctly

### Step 21 ✅ — Supermarket REST controller (POST + GET by ID + GET list)
- Create `supermarket/infrastructure/adapter/input/rest/SupermarketController.java`
- Create `supermarket/infrastructure/adapter/input/rest/dto/RegisterSupermarketRequest.java` (with Bean Validation + OpenAPI `@Schema`)
- Create `supermarket/infrastructure/adapter/input/rest/dto/SupermarketResponse.java` (record)
- `POST /api/v1/supermarkets` → 201 Created + Location header
- `GET /api/v1/supermarkets/{id}` → 200 / 404
- `GET /api/v1/supermarkets?page=0&size=20` → paginated list
- **Verify:** `@WebMvcTest` covering happy paths + 404 scenario

### Step 22 ✅ — Seed initial Supermarket data (Flyway)
- Create `V3__seed_supermarkets.sql` inserting: Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA
- **Verify:** `./gradlew bootRun` + `GET /api/v1/supermarkets` returns 6 results

