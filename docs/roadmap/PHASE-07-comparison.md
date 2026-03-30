# Phase 7 — Comparison Bounded Context

> **Steps 62–66** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 62 ✅ — Define ProductComparison domain model
- Create `comparison/domain/model/ComparisonId.java` (UUID record)
- Create `comparison/domain/model/ProductMatch.java` (Value Object — `productId`, `supermarketId`, `productName`, `latestPrice`, `priceRecordedAt`)
- Create `comparison/domain/model/ProductComparison.java` (Value Object — `searchTerm`, `List<ProductMatch> matches`, `ComparisonId`)
  - `cheapest()` → returns `Optional<ProductMatch>` with the lowest `latestPrice`
  - `sortedByPrice()` → returns `List<ProductMatch>` ascending by `latestPrice`
- **Verify:** unit tests on `cheapest()` + `sortedByPrice()` with multiple matches, single match, ties

### Step 63 ✅ — Define Comparison output ports + DTOs
- Create `comparison/application/port/output/ProductComparisonQueryPort.java`
  - `List<ProductMatch> findMatchesByName(String searchTerm, List<UUID> supermarketIds)`
- Create `comparison/application/dto/ProductComparisonView.java` (record — `searchTerm`, `matches` sorted by price, `cheapestSupermarketId`, `cheapestSupermarketName`)
- Create `comparison/application/dto/ProductMatchView.java` (record — `productId`, `supermarketId`, `supermarketName`, `productName`, `unitPrice`, `bulkPrice`, `referencePrice`, `referenceFormat`, `priceRecordedAt`)
- Create `comparison/application/dto/CompareProductsByNameQuery.java` (record — `searchTerm`, `List<UUID> supermarketIds`)
- **Verify:** compile only

### Step 64 ✅ — Compare Products by name use case (query)
- Create `comparison/application/port/input/query/CompareProductsByNameUseCase.java`
- Create `comparison/application/query/CompareProductsByNameHandler.java`
  - Calls `ProductComparisonQueryPort.findMatchesByName()`
  - Builds `ProductComparison` and maps to `ProductComparisonView`
- **Verify:** unit tests — multiple matches (sorted + cheapest correct), single match, no matches (empty view + null cheapest)

### Step 65 ✅ — ProductComparison query implementation (SQL)
- Create `comparison/infrastructure/adapter/output/persistence/ProductComparisonJdbcAdapter.java`
  - Uses `JdbcClient` (Spring 6.1+) with a native SQL query
  - `LATERAL` subquery for latest price per product (no `DISTINCT ON` — compatible with all Postgres versions)
  - `ILIKE '%:term%'` search on product name; optional `= ANY(:supermarkets)` filter
- Create `V9__add_product_name_search_index.sql`: `pg_trgm` extension + GIN index on `products.name`
- **Verify:** `@SpringBootTest` + Testcontainers integration test:
  - Search "aceite" returns 2 products from 2 supermarkets, sorted by price ASC
  - Filter by single supermarket returns only that supermarket's products
  - Empty search returns empty list
  - ILIKE is case-insensitive
- **Fix:** test helper used wrong column name `level` — corrected to `level_type`

### Step 66 ✅ — Comparison Config + REST controller
- Create `ComparisonConfig.java` (bean registration — `JdbcClient` auto-configured by Spring Boot)
- Create `ComparisonController.java`:
  - `GET /api/v1/compare?q=leche&supermarkets=uuid1,uuid2`
  - Returns `ProductComparisonView` (sorted matches + cheapest supermarket info)
  - Missing `q` → 400 (handled by `GlobalExceptionHandler`)
- **Verify:** `MockMvc` — happy path (200 + sorted), empty results (200), missing q (400)

