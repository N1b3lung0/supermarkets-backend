# Phase 2 — Category Bounded Context

> **Steps 23–30** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

## Context — Real API structure (3 levels)

**Level 0 (top category):** `GET /api/categories/` → `results[]` — e.g. id=`12` "Aceite, especias y salsas"
Fields: `id` (int), `name`, `order`, `is_extended`. Contains `categories[]` (level-1 list, no products).

**Level 1 (subcategory):** inside `results[].categories[]` — e.g. id=`112` "Aceite, vinagre y sal"
Fields: `id`, `name`, `order`, `layout`, `published`, `is_extended`. No products here either.

**Level 2 (leaf subcategory):** `GET /api/categories/{level1Id}` → `categories[]` — e.g. id=`420` "Aceite de oliva"
Fields: `id`, `name`, `order`, `layout`, `published`, `is_extended`, `image` (nullable), `subtitle` (nullable), `products[]`.
**Products live here** — each product has full `price_instructions`, `thumbnail`, `badges`, `packaging`, `display_name`.

**Scraping strategy:**
1. Fetch level-0 + level-1 from `GET /api/categories/`
2. For each level-1 id, call `GET /api/categories/{level1Id}` to get level-2 groups with their products
3. Products in this response are **complete** — no separate `/api/products/{id}` call needed

The domain models **all 3 levels as `Category`** using the same self-referencing structure (`parentCategoryId`).
Level-2 categories are the **leaf nodes** — only they are directly associated with products.

See also: [Category level mapping](APPENDIX.md#category-level-mapping)

---

### Step 23 ✅ — Define Category domain model
- Create `category/domain/model/CategoryId.java` (record, UUID)
- Create `category/domain/model/ExternalCategoryId.java` (record — wraps the Mercadona integer id as `String`, e.g. `"12"`, `"112"`, `"420"`)
- Create `category/domain/model/CategoryName.java` (record, non-blank, max 255)
- Create `category/domain/model/CategoryLevel.java` (enum: `TOP(0)`, `SUBCATEGORY(1)`, `LEAF(2)` — maps to `level` concept; `LEAF` is the only level that has products)
- Create `category/domain/model/Category.java` (Aggregate Root):
  - `CategoryId id`
  - `ExternalCategoryId externalId`
  - `CategoryName name`
  - `SupermarketId supermarketId`
  - `CategoryId parentCategoryId` (nullable — null for level-0 top categories)
  - `CategoryLevel level`
  - `int displayOrder` (maps to `order`)
  - `boolean published` (present on level-1 and level-2 only; default `true` for level-0)
  - `boolean isLeaf()` — convenience method returning `level == LEAF`
  - `create(...)` static factory — emits `CategorySynced`
  - `update(name, order)` — emits `CategorySynced` only if changed
  - `pullDomainEvents()`
- **Verify:** unit tests on `create()` + `update()` — happy path, invariants, `isLeaf()` returns correct value per level

### Step 24 ✅ — Category domain events + exceptions
- Create `category/domain/event/CategoryEvent.java` (sealed interface — `permits CategorySynced`)
- Create `category/domain/event/CategorySynced.java` (record — `CategoryId id`, `ExternalCategoryId externalId`, `SupermarketId supermarketId`, `CategoryLevel level`, `Instant occurredOn`)
- Create `category/domain/exception/CategoryNotFoundException.java` (extends `NotFoundException`)
- **Verify:** unit test asserting `Category.create()` emits `CategorySynced` with correct level

### Step 25 ✅ — Category output ports + DTOs
- Create `category/application/port/output/CategoryRepositoryPort.java`
  - `save(Category)`, `findById(CategoryId)`, `findByExternalIdAndSupermarket(ExternalCategoryId, SupermarketId)`, `deleteById(CategoryId)`
- Create `category/application/port/output/CategoryQueryPort.java`
  - `findDetailById(CategoryId)` → `Optional<CategoryDetailView>`
  - `findTopLevelBySupermarket(SupermarketId, Pageable)` → `Page<CategorySummaryView>` (level-0 only)
  - `findLeafCategoriesBySupermarket(SupermarketId)` → `List<CategorySummaryView>` (level-2 only — used by sync to iterate products)
- Create `category/application/dto/CategoryDetailView.java` (record — `id`, `externalId`, `name`, `level`, `parentCategoryId` nullable, `children: List<CategorySummaryView>`)
- Create `category/application/dto/CategorySummaryView.java` (record — `id`, `externalId`, `name`, `level`, `supermarketId`, `parentCategoryId` nullable)
- **Verify:** compile only

### Step 26 ✅ — Upsert Category use case (command)
- Create `category/application/port/input/command/UpsertCategoryUseCase.java`
- Create `category/application/dto/UpsertCategoryCommand.java` (record):
  ```
  ExternalCategoryId externalId, CategoryName name, SupermarketId supermarketId,
  ExternalCategoryId parentExternalId (nullable), CategoryLevel level, int displayOrder, boolean published
  ```
- Create `category/application/command/UpsertCategoryHandler.java`
  - Looks up by `(externalId, supermarketId)`; if parent present, resolves `parentCategoryId` from `(parentExternalId, supermarketId)`
  - Create if not exists; update `name` + `displayOrder` if changed; no-op otherwise
  - Always emits `CategorySynced` on create; only on actual change on update
- **Verify:** unit tests — create level-0, create level-1 with parent, create level-2 with parent, update name (event emitted), update with same data (no event)

### Step 27 ✅ — Get Category + List Categories use cases (query)
- Create `category/application/port/input/query/GetCategoryByIdUseCase.java` + `GetCategoryByIdQuery.java` + `GetCategoryByIdHandler.java` (returns `CategoryDetailView` with children)
- Create `category/application/port/input/query/ListTopCategoriesBySupermarketUseCase.java` + `ListTopCategoriesBySupermarketQuery.java` + `ListTopCategoriesBySupermarketHandler.java` (level-0 only, paginated)
- **Verify:** unit tests with mocked `CategoryQueryPort` — found, not found (`CategoryNotFoundException`), paginated list

### Step 28 ✅ — Category JPA Entity + Flyway migration
- Create `V4__create_categories_table.sql`:
  ```sql
  CREATE TABLE categories (
    id                UUID         PRIMARY KEY,
    external_id       VARCHAR(20)  NOT NULL,
    name              VARCHAR(255) NOT NULL,
    supermarket_id    UUID         NOT NULL REFERENCES supermarkets(id),
    parent_category_id UUID        REFERENCES categories(id),
    level             SMALLINT     NOT NULL,   -- 0=TOP, 1=SUBCATEGORY, 2=LEAF
    display_order     INTEGER      NOT NULL DEFAULT 0,
    published         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT uq_category_external_supermarket UNIQUE (external_id, supermarket_id)
  );
  CREATE INDEX idx_categories_supermarket_level ON categories (supermarket_id, level);
  CREATE INDEX idx_categories_parent ON categories (parent_category_id);
  ```
- Create `category/infrastructure/adapter/output/persistence/entity/CategoryEntity.java`
  - `@ManyToOne(fetch = LAZY)` self-reference for `parentCategoryId`
  - `@SQLRestriction("deleted_at IS NULL")` + `@SQLDelete`
  - `@EntityListeners(AuditingEntityListener.class)`
- **Verify:** Flyway migration runs clean; JPA schema validates

### Step 29 ✅ — Category persistence mapper + Spring repository + JPA adapter
- Create `SpringCategoryRepository.java`:
  - `findByExternalIdAndSupermarketId(String, UUID)` → `Optional<CategoryEntity>`
  - `findByExternalIdAndSupermarketIdAndLevel(String, UUID, int)` → to safely resolve parent refs
  - `findBySupermarketIdAndLevelOrderByDisplayOrder(UUID, int, Pageable)` → paginated by level
  - `findBySupermarketIdAndLevel(UUID, int)` → full list of leaf categories (for sync iteration)
- Create `CategoryPersistenceMapper.java` — `toDomain(CategoryEntity)`, `toEntity(Category)` (no circular parent loading — parent resolved by id only)
- Create `CategoryJpaAdapter.java` implementing `CategoryRepositoryPort` + `CategoryQueryPort`
  - `findDetailById`: loads entity + children via `@OneToMany(mappedBy = "parent")` with `@EntityGraph`
- **Verify:** `@DataJpaTest` + Testcontainers — upsert level-0, upsert level-1 with parent, upsert level-2; query leaf categories; soft delete

### Step 30 ✅ — Category Config + REST controller
- Create `CategoryConfig.java` (bean registration with `TransactionTemplate`)
- Create `CategoryController.java`:
  - `GET /api/v1/supermarkets/{supermarketId}/categories?page=0&size=20` → `PageResponse<CategorySummaryView>` (top-level only)
  - `GET /api/v1/categories/{id}` → `CategoryDetailView` (with children list)
- REST DTOs: `CategoryDetailResponse.java`, `CategorySummaryResponse.java` with `@Schema` annotations
- **Verify:** `@WebMvcTest` — top-level list, detail with children, 404

