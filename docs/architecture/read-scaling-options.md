# Read Scaling Options

Architecture decision record for the read-path scaling strategy of the supermarket price comparator.

---

## Context

The application has two primary read hot-paths:

| Query | Typical cardinality | Freshness requirement |
|---|---|---|
| `GET /api/v1/comparisons?name=…` | Scans all active products across 6 supermarkets | Stale-while-revalidate acceptable (price syncs once/day) |
| `GET /api/v1/products/{id}` | Single product with latest price | Same — stale-within-30min acceptable |

Both paths are **read-heavy** and **write-rarely** (writes happen only during the nightly sync).

---

## Options Evaluated

### Option A — LATERAL Subquery on Every Request

```sql
SELECT p.*, pp.*
FROM products p
JOIN LATERAL (
    SELECT * FROM product_prices
    WHERE product_id = p.id
    ORDER BY recorded_at DESC LIMIT 1
) pp ON TRUE
WHERE p.name ILIKE '%aceite%' AND p.is_active = TRUE
```

**Pros:**
- Always returns fresh data.
- No extra maintenance step.

**Cons:**
- Executes an expensive correlated subquery for every matched product on every request.
- With millions of price rows the LATERAL scan is slow even with indexes.
- Under concurrent load the query planner may choose sequential scans.

**Decision: Rejected** for the comparison endpoint (used as fallback in dev).

---

### Option B — Materialized View `latest_product_prices` ✅ (Selected)

A pre-computed snapshot of the latest price per product, refreshed after every sync:

```sql
CREATE MATERIALIZED VIEW latest_product_prices AS
SELECT DISTINCT ON (product_id)
    id, product_id, unit_price, bulk_price, reference_price,
    reference_format, currency, recorded_at
FROM product_prices
ORDER BY product_id, recorded_at DESC;

CREATE UNIQUE INDEX idx_latest_product_prices_product_id
    ON latest_product_prices (product_id);
```

The comparison query becomes a simple equi-join:

```sql
SELECT p.id, p.name, p.supermarket_id, ...
    pp.unit_price, pp.currency
FROM products p
JOIN latest_product_prices pp ON pp.product_id = p.id
WHERE p.name ILIKE '%aceite%'
  AND p.is_active = TRUE
  AND (:supermarketIds IS NULL OR p.supermarket_id = ANY(:supermarketIds))
ORDER BY pp.unit_price ASC
```

**Pros:**
- O(1) lookup per matched product (unique index on `product_id`).
- No correlated subquery: single sequential-then-indexed join.
- Refresh is `CONCURRENTLY` safe — does not block reads.
- Perfectly aligned with sync cadence (once per day per supermarket).

**Cons:**
- Data is stale between syncs (acceptable — prices update once/day).
- Adds a `REFRESH MATERIALIZED VIEW CONCURRENTLY` step to the sync pipeline.
- `CONCURRENTLY` requires a unique index (already present).

**Decision: Selected.** Refresh is triggered by `LatestPricesRefreshJdbcAdapter` at the end of `SyncSupermarketCatalogHandler`.

---

### Option C — Redis Cache on Application Layer

Cache comparison results keyed by `(searchTerm, sorted supermarket UUIDs)` with a 1-hour TTL.

**Pros:**
- Sub-millisecond response for repeated identical queries.
- Offloads DB entirely for cached combinations.

**Cons:**
- Cache is per-key: a new search term misses until warmed.
- Requires Redis availability in production.
- Cache invalidation complexity: must evict all compare keys after each sync.

**Decision: Complementary layer on top of Option B.** Implemented as `CachingCompareProductsByNameUseCase` (infrastructure decorator, no Spring annotations in domain/application). Eviction is driven by `ProductSynced` domain events via `ProductCacheEvictionListener`.

---

### Option D — Read Replica / CQRS at DB Level

Route all read queries to a PostgreSQL read replica. Writes (sync) go to primary, reads go to replica.

**Pros:**
- Removes read load from primary entirely.
- Scales horizontally by adding more replicas.

**Cons:**
- Adds operational complexity (replica lag monitoring, connection pool config).
- Replication lag can cause stale reads (already accepted for Option B).
- Overkill at current data volumes (< 1M products, < 6 supermarkets).

**Decision: Future option.** Revisit when p95 read latency exceeds 100 ms under production load. The hexagonal port (`CompareProductsByNamePort`) is already DB-agnostic so swapping the adapter requires no application or domain changes.

---

### Option E — Denormalised `products` Table (embed latest price)

Embed `unit_price`, `currency` directly into the `products` row, updated on every sync.

**Pros:**
- Comparison query reduces to a single table scan.
- Simplest possible query.

**Cons:**
- Violates normalisation: price history is lost in the main table.
- Upsert logic becomes a two-phase write (product + inline price), harder to keep atomic.
- Conflicts with the `product_prices` history requirement.

**Decision: Rejected.** Price history is a first-class requirement.

---

## Current Architecture

```
Request
  │
  ▼
ComparisonController
  │  inject
  ▼
CachingCompareProductsByNameUseCase   ← Redis L1 (1 h TTL, evicted on sync)
  │  cache miss
  ▼
CompareProductsByNameHandler
  │
  ▼
ProductComparisonJdbcAdapter
  │  SQL JOIN
  ▼
latest_product_prices (mat. view)     ← PostgreSQL L2 (refreshed post-sync)
  +
products (indexed on name, active)
```

---

## Performance Indexes (V12)

```sql
-- Covers comparison query WHERE clause + ORDER BY
CREATE INDEX idx_products_supermarket_active
    ON products (supermarket_id) WHERE is_active = TRUE AND deleted_at IS NULL;

-- Speeds up external-id lookups during sync upsert
CREATE INDEX idx_products_external_supermarket
    ON products (external_id, supermarket_id) WHERE deleted_at IS NULL;

-- Covers category-scoped product list queries
CREATE INDEX idx_products_supermarket_category
    ON products (supermarket_id, category_id) WHERE deleted_at IS NULL;
```

---

## Partition Strategy for `product_prices` (V13)

`product_prices` is declared `PARTITION BY RANGE(recorded_at)` with quarterly child partitions.
`PartitionMaintenanceJdbcAdapter` is called at the start of each sync to ensure the next month's
partition exists, preventing INSERT failures near partition boundaries.

---

## Metrics to Monitor

| Metric | Alert threshold |
|---|---|
| `latest_product_prices` row count | < expected total active products |
| Comparison query p95 latency | > 200 ms |
| Product detail cache hit rate | < 80% |
| Materialized view refresh duration | > 60 s |
| Redis memory usage | > 80% `maxmemory` |

