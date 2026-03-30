# Phase 5 — Sync Use Case (Orchestration)

> **Steps 53–59** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 53 ✅ — Define SyncSupermarketCatalogUseCase
- Create `sync/application/port/input/command/SyncSupermarketCatalogUseCase.java`
- Create `sync/application/dto/SyncSupermarketCatalogCommand.java` (record — `supermarketId`)
- **Verify:** compile only

### Step 54 ✅ — Implement SyncSupermarketCatalogHandler (categories)
- Create `sync/application/command/SyncSupermarketCatalogHandler.java`
  - Calls `CategoryScraperPort.fetchCategories(supermarketId)`
  - For each `UpsertCategoryCommand`, calls `UpsertCategoryUseCase`
  - Logs progress (total fetched, total upserted)
- **Verify:** unit test with mocked scraper port and use case — asserts all categories processed

### Step 55 ✅ — Extend SyncSupermarketCatalogHandler (products + prices)
- Extend handler from Step 54 to also sync products:
  1. Load all level-1 (SUBCATEGORY) external ids from the previously synced categories
  2. Build `leafCategoryIndex`: `Map<ExternalCategoryId, CategoryId>` by querying all LEAF categories from DB
  3. For each level-1 subcategory external id, call `ProductScraperPort.fetchProductsBySubcategory(supermarketId, level1ExternalId, leafCategoryIndex)`
  4. Collect all scraped external product ids; for each `UpsertProductCommand` call `UpsertProductUseCase` (which internally records the price)
  5. Diff: load `ProductRepositoryPort.findActiveExternalIdsBySupermarket()`, subtract scraped ids → call `DeactivateProductUseCase` for missing products
- Design rationale: only ~100 HTTP calls (one per level-1 subcategory) to fetch all ~10,000 Mercadona products — no per-product calls needed in the main sync
- **Verify:** unit tests per scenario:
  - New products: saved + price recorded + `ProductSynced` emitted
  - Existing product with changed name: updated + price recorded
  - Existing product unchanged: not saved + price still recorded (daily snapshot)
  - Product absent from current scrape: `DeactivateProductUseCase` called

### Step 56 ✅ — Create Sync Config + REST trigger endpoint
- Create `sync/infrastructure/config/SyncConfig.java`
- Create `sync/infrastructure/adapter/input/rest/SyncController.java`
  - `POST /api/v1/sync/supermarkets/{supermarketId}` → triggers sync, returns `202 Accepted`
- **Verify:** `@WebMvcTest` verifying 202 response; integration test triggering full sync against Testcontainers DB

### Step 57 ✅ — Add SyncRun audit entity (track sync executions)
- Create `V7__create_sync_runs_table.sql`:
  ```sql
  CREATE TABLE sync_runs (
    id                   UUID         PRIMARY KEY,
    supermarket_id       UUID         NOT NULL REFERENCES supermarkets(id),
    started_at           TIMESTAMPTZ  NOT NULL,
    finished_at          TIMESTAMPTZ,
    status               VARCHAR(20)  NOT NULL,  -- RUNNING, COMPLETED, FAILED
    categories_synced    INTEGER      NOT NULL DEFAULT 0,
    products_synced      INTEGER      NOT NULL DEFAULT 0,
    products_deactivated INTEGER      NOT NULL DEFAULT 0,
    error_message        TEXT
  );
  ```
- Create `sync/domain/model/SyncRunId.java` (record, UUID)
- Create `sync/domain/model/SyncStatus.java` (enum: `RUNNING`, `COMPLETED`, `FAILED`)
- Create `sync/domain/model/SyncRun.java` (Aggregate Root)
  - `start(supermarketId)` static factory → status = `RUNNING`
  - `complete(categoriesSynced, productsSynced, productsDeactivated)` → status = `COMPLETED`
  - `fail(errorMessage)` → status = `FAILED`
- **Verify:** migration runs; unit tests on `SyncRun` state transitions

### Step 58 ✅ — Integrate SyncRun into SyncSupermarketCatalogHandler
- Handler creates a `SyncRun` at start → persists with `RUNNING`
- On completion → updates to `COMPLETED` with counters
- On exception → updates to `FAILED` with error message
- Create `sync/application/port/output/SyncRunRepositoryPort.java`
- **Verify:** unit tests asserting `SyncRun` is saved with correct status and counters in all three scenarios

### Step 59 ✅ — SyncRun JPA persistence + query endpoint
- Create `sync/infrastructure/adapter/output/persistence/entity/SyncRunEntity.java`
- Create `sync/infrastructure/adapter/output/persistence/repository/SpringSyncRunRepository.java`
- Create `sync/infrastructure/adapter/output/persistence/SyncRunJpaAdapter.java`
- Create `GET /api/v1/sync/runs?supermarketId=&page=0&size=20` → `PageResponse<SyncRunView>` ordered by `startedAt DESC`
- **Verify:** `@DataJpaTest` save/find; `@WebMvcTest` pagination + filter by supermarketId
- **Fixes applied:** corrected `createSupermarket()` SQL helper to match real schema; added `PageableHandlerMethodArgumentResolver` to MockMvc setup; added `MissingServletRequestParameterException` → 400 handler in `GlobalExceptionHandler`

