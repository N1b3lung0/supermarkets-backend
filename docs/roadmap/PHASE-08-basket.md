# Phase 8 — Basket Bounded Context

> **Steps 67–73** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 67 ✅ — Define Basket domain model
- Create `basket/domain/model/BasketId.java`, `BasketItemId.java` (UUID records)
- Create `basket/domain/model/BasketItem.java` (entity — `id`, `productName`, `quantity`; `reconstitute()` public for mapper)
- Create `basket/domain/model/Basket.java` (Aggregate Root — `create`, `addItem`, `removeItem`, `updateItemQuantity`, `clear`, `pullDomainEvents`)
- **Verify:** `BasketDomainTest` — all business methods + invariants + event emission

### Step 68 ✅ — Basket domain events + exceptions
- Create `basket/domain/event/BasketEvent.java` (sealed interface — permits `BasketCreated`, `BasketItemAdded`, `BasketItemRemoved`)
- Create `BasketCreated`, `BasketItemAdded`, `BasketItemRemoved` event records
- Create `BasketNotFoundException`, `BasketItemNotFoundException`, `DuplicateBasketItemException`

### Step 69 ✅ — Basket use cases (commands)
- `CreateBasket`, `AddBasketItem`, `RemoveBasketItem`, `UpdateBasketItemQuantity`, `ClearBasket` — use case interfaces + DTOs + handlers
- **Verify:** `BasketCommandHandlersTest` — happy paths + `BasketNotFoundException` scenarios

### Step 70 ✅ — Basket use cases (queries)
- `GetBasketByIdUseCase` + `GetBasketByIdQuery` + `GetBasketByIdHandler`
- `BasketDetailView` + `BasketItemView` DTOs
- **Verify:** covered by `BasketControllerTest` + `BasketJpaAdapterTest`

### Step 71 ⬜ — Compare Basket use case (query)
- Create `basket/application/port/input/query/CompareBasketUseCase.java`
- Create `basket/application/dto/CompareBasketQuery.java` (record — `BasketId`)
- Create `basket/application/dto/BasketComparisonView.java` (record):
  - `List<SupermarketBasketCost> perSupermarket` — each with supermarketId, supermarketName, totalCost, perItemMatches
  - `SupermarketId cheapestSupermarketId` (nullable — null if no prices found)
- Create `basket/application/query/CompareBasketHandler.java`
  - For each item in basket, calls `ProductComparisonQueryPort` (cross-context via output port)
  - Aggregates results per supermarket: sums cheapest matching product price × quantity
- **Verify:** unit tests — basket with 3 items, 2 supermarkets — correct total per supermarket, correct cheapest identified

### Step 72 ✅ — Basket JPA persistence + Flyway migration
- Create `V10__create_baskets_table.sql` (baskets + basket_items with CASCADE + unique constraint on product_name per basket)
- Create `BasketEntity`, `BasketItemEntity` (@OneToMany orphanRemoval)
- Create `SpringBasketRepository` (findByIdWithItems JPQL LEFT JOIN FETCH)
- Create `BasketJpaAdapter` + `BasketPersistenceMapper`
- **Verify:** `BasketJpaAdapterTest` (Testcontainers) — round-trip, add/remove items, not-found

### Step 73 ✅ — Basket Config + REST controller
- Create `BasketConfig.java` (all bean wiring, zero Spring annotations in domain/application)
- Create `BasketController.java`:
  - `POST /api/v1/baskets` → 201 + Location
  - `GET /api/v1/baskets/{id}` → `BasketDetailView` (404 if not found)
  - `POST /api/v1/baskets/{id}/items` → 201 + Location
  - `PATCH /api/v1/baskets/{id}/items/{itemId}` → 204
  - `DELETE /api/v1/baskets/{id}/items/{itemId}` → 204
  - `DELETE /api/v1/baskets/{id}/items` → 204 (clear all)
- **Verify:** `BasketControllerTest` — all endpoints + 422 validation + 404 scenarios
- **Fixes:** `BasketEntity`/`BasketItemEntity` constructors made public for cross-package mapper access

