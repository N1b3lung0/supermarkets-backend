# Phase 10 — Performance & Data Strategy

> **Steps 81–86** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 81 ✅ — Add Redis cache for comparison queries
- Add `spring-boot-starter-data-redis` to `libs.versions.toml`
- Add Redis service to `compose.yaml`
- Create `shared/infrastructure/config/CacheConfig.java` with TTL per cache name:
  - `compare` cache: 1 hour TTL
  - `product` cache: 30 minutes TTL
- Cache `CompareProductsByNameHandler` results with key `compare:{searchTerm}:{supermarketIds}`
- Cache `GetProductByIdHandler` with key `product:{id}`
- **Verify:** integration test asserting cache hit/miss; `./gradlew bootRun` with Redis running

### Step 82 ✅ — Add cache eviction on product sync
- When `ProductSynced` or `ProductDeactivated` domain event is published, evict the relevant cache entries
- Create `product/infrastructure/adapter/input/event/ProductCacheEvictionListener.java`
  - `@EventListener` on `ProductSynced` → evict `product:{id}` + clear `compare` cache
  - `@EventListener` on `ProductDeactivated` → evict `product:{id}`
- **Verify:** unit test asserting cache eviction called on each event type

### Step 83 ✅ — Add database indexes for common queries
- Create `V12__add_performance_indexes.sql`:
  ```sql
  CREATE INDEX idx_products_supermarket_category ON products (supermarket_id, category_id)
    WHERE deleted_at IS NULL;
  CREATE INDEX idx_products_supermarket_active ON products (supermarket_id)
    WHERE is_active = TRUE AND deleted_at IS NULL;
  -- product_prices index already created in V6; confirm it exists
  -- pg_trgm index already created in V9; confirm it exists
  ```
- **Verify:** migration runs; `EXPLAIN ANALYZE` on comparison query shows index usage

### Step 84 ✅ — Partition product_prices table (time-based)
- Create `V13__partition_product_prices_by_month.sql`:
  - Convert `product_prices` to a `PARTITION BY RANGE (recorded_at)` table
  - Create initial monthly partitions for current + next 12 months
- Create `sync/infrastructure/adapter/output/persistence/PartitionMaintenanceAdapter.java`
  - Called at the start of each sync to create the next month's partition if it doesn't exist
- **Verify:** migration runs; existing data accessible; new prices insert into correct partition

### Step 85 ✅ — Evaluate and document read scaling options
- Create `docs/architecture/read-scaling-options.md` comparing:
  - **PostgreSQL + Redis** (current): good for < 1M products × 6 supermarkets
  - **Read replicas**: offload comparison queries to replica
  - **Elasticsearch**: full-text product search + aggregations (recommended for > 10M records)
  - **Materialized views**: pre-computed latest prices per product/supermarket
- Implement **materialized view** for latest prices:
  ```sql
  -- V14__create_latest_product_prices_view.sql
  CREATE MATERIALIZED VIEW latest_product_prices AS
    SELECT DISTINCT ON (product_id) *
    FROM product_prices
    ORDER BY product_id, recorded_at DESC;
  CREATE UNIQUE INDEX ON latest_product_prices (product_id);
  ```
- Update `ProductComparisonJdbcAdapter` to use the materialized view instead of the subquery
- Add refresh call in `SyncSupermarketCatalogHandler.complete()`
- **Verify:** comparison query response time < 100ms with 100k products seed data

### Step 86 ✅ — Add Testcontainers-based performance smoke test
- Seed 10,000 products across 6 supermarkets using a Flyway test-only migration
- Assert `GET /api/v1/compare?q=leche` responds in < 200ms (measured with `StopWatch`)
- Assert `GET /api/v1/products/{id}` responds in < 50ms (cache hit)
- **Verify:** test passes on CI
