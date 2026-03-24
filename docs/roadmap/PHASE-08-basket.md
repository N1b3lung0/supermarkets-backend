# Phase 8 — Basket Bounded Context

> **Steps 67–73** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 67 ⬜ — Define Basket domain model
- Create `basket/domain/model/BasketId.java`, `BasketItemId.java` (UUID records)
- Create `basket/domain/model/BasketItem.java` (entity — `id`, `productName`, `quantity`)
- Create `basket/domain/model/Basket.java` (Aggregate Root — `id`, `name`, `List<BasketItem> items`)
  - `create(name)` factory
  - `addItem(productName, quantity)` — enforces no duplicates by name → throws `DuplicateBasketItemException`
  - `removeItem(BasketItemId)` — throws `BasketItemNotFoundException` if not found
  - `updateItemQuantity(BasketItemId, int)` — throws `BasketItemNotFoundException`; quantity must be > 0
  - `clear()` — removes all items
- **Verify:** unit tests on all business methods + invariants

### Step 68 ⬜ — Basket domain events + exceptions
- Create `basket/domain/event/BasketEvent.java` (sealed interface)
- Create `basket/domain/event/BasketCreated.java`, `BasketItemAdded.java`, `BasketItemRemoved.java` (records)
- Create `basket/domain/exception/BasketNotFoundException.java` (extends `NotFoundException`)
- Create `basket/domain/exception/BasketItemNotFoundException.java` (extends `NotFoundException`)
- Create `basket/domain/exception/DuplicateBasketItemException.java` (extends `ConflictException`)
- **Verify:** unit tests per event emission

### Step 69 ⬜ — Basket use cases (commands)
- `CreateBasketUseCase` + `CreateBasketCommand` + `CreateBasketHandler`
- `AddBasketItemUseCase` + `AddBasketItemCommand` + `AddBasketItemHandler`
- `RemoveBasketItemUseCase` + `RemoveBasketItemCommand` + `RemoveBasketItemHandler`
- `UpdateBasketItemQuantityUseCase` + `UpdateBasketItemQuantityCommand` + `UpdateBasketItemQuantityHandler`
- `ClearBasketUseCase` + `ClearBasketCommand` + `ClearBasketHandler`
- **Verify:** unit tests per handler — happy path + `BasketNotFoundException` + `BasketItemNotFoundException` scenarios

### Step 70 ⬜ — Basket use cases (queries)
- `GetBasketByIdUseCase` + `GetBasketByIdQuery` + `GetBasketByIdHandler`
- Create `basket/application/dto/BasketDetailView.java` (record — `id`, `name`, `items: List<BasketItemView>`)
- Create `basket/application/dto/BasketItemView.java` (record — `id`, `productName`, `quantity`)
- **Verify:** unit test returning `BasketDetailView`; `BasketNotFoundException` when not found

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

### Step 72 ⬜ — Basket JPA persistence + Flyway migration
- Create `V10__create_baskets_table.sql` + `V11__create_basket_items_table.sql`
- Create `basket/infrastructure/adapter/output/persistence/entity/BasketEntity.java`
- Create `basket/infrastructure/adapter/output/persistence/entity/BasketItemEntity.java`
- Create `basket/infrastructure/adapter/output/persistence/repository/SpringBasketRepository.java`
- Create `basket/infrastructure/adapter/output/persistence/BasketJpaAdapter.java`
- **Verify:** `@DataJpaTest` + Testcontainers — CRUD + soft delete + item management

### Step 73 ⬜ — Basket Config + REST controller
- Create `BasketConfig.java` (bean registration)
- Create `BasketController.java`:
  - `POST /api/v1/baskets` → 201 + Location header
  - `GET /api/v1/baskets/{id}` → `BasketDetailView`
  - `POST /api/v1/baskets/{id}/items` → 201
  - `DELETE /api/v1/baskets/{id}/items/{itemId}` → 204
  - `PATCH /api/v1/baskets/{id}/items/{itemId}` → 200 (update quantity)
  - `DELETE /api/v1/baskets/{id}/items` → 204 (clear)
  - `GET /api/v1/baskets/{id}/compare` → `BasketComparisonView`
- **Verify:** `@WebMvcTest` all endpoints — happy paths + 404 scenarios

