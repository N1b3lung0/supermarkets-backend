# Phase 7 — Comparison Bounded Context

> **Steps 62–66** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 62 ⬜ — Define ProductComparison domain model
- Create `comparison/domain/model/ComparisonId.java` (UUID record)
- Create `comparison/domain/model/ProductMatch.java` (Value Object — `productId`, `supermarketId`, `productName`, `latestPrice`, `priceRecordedAt`)
- Create `comparison/domain/model/ProductComparison.java` (Value Object — `searchTerm`, `List<ProductMatch> matches`, `ComparisonId`)
  - `cheapest()` → returns `Optional<ProductMatch>` with the lowest `latestPrice`
  - `sortedByPrice()` → returns `List<ProductMatch>` ascending by `latestPrice`
- **Verify:** unit tests on `cheapest()` + `sortedByPrice()` with multiple matches, single match, ties

### Step 63 ⬜ — Define Comparison output ports + DTOs
- Create `comparison/application/port/output/ProductComparisonQueryPort.java`
  - `List<ProductMatch> findMatchesByName(String searchTerm, List<SupermarketId> supermarketIds)`
- Create `comparison/application/dto/ProductComparisonView.java` (record — `searchTerm`, `matches` sorted by price, `cheapestSupermarket` nullable)
- Create `comparison/application/dto/ProductMatchView.java` (record — `productId`, `supermarketId`, `supermarketName`, `productName`, `unitPrice`, `bulkPrice`, `referencePrice`, `referenceFormat`, `priceRecordedAt`)
- **Verify:** compile only

### Step 64 ⬜ — Compare Products by name use case (query)
- Create `comparison/application/port/input/query/CompareProductsByNameUseCase.java`
- Create `comparison/application/dto/CompareProductsByNameQuery.java` (record — `searchTerm`, `List<SupermarketId> supermarketIds`)
- Create `comparison/application/query/CompareProductsByNameHandler.java`
  - Calls `ProductComparisonQueryPort.findMatchesByName()`
  - Builds `ProductComparison` and maps to `ProductComparisonView`
- **Verify:** unit tests — multiple matches, single match, no matches (empty view returned, not 404)

### Step 65 ⬜ — ProductComparison query implementation (SQL)
- Create `comparison/infrastructure/adapter/output/persistence/ProductComparisonJdbcAdapter.java`
  - Uses `JdbcClient` (Spring 6.1+) with a native SQL query
  - Joins `products` with latest price from `product_prices` (using `DISTINCT ON (product_id) ORDER BY recorded_at DESC`)
  - `ILIKE '%:term%'` search on product name; filter by supermarket ids if provided
- Create `V9__add_product_name_search_index.sql`:
  ```sql
  CREATE EXTENSION IF NOT EXISTS pg_trgm;
  CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);
  ```
- **Verify:** `@DataJpaTest` + Testcontainers with seed data:
  - Search "aceite" returns products from multiple supermarkets
  - Results ordered by price ascending
  - Filter by single supermarket returns only that supermarket's products

### Step 66 ⬜ — Comparison Config + REST controller
- Create `ComparisonConfig.java` (bean registration)
- Create `ComparisonController.java`:
  - `GET /api/v1/compare?q=leche&supermarkets=mercadona,carrefour&page=0&size=10`
  - Returns `PageResponse<ProductComparisonView>`
- **Verify:** `@WebMvcTest` happy path + empty results (200, not 404) + missing `q` → 400

