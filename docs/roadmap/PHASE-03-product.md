# Phase 3 — Product Bounded Context

> **Steps 31–46** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

## Context — API field mapping

`GET https://tienda.mercadona.es/api/products/3400` (enrichment only — see [Phase 4](PHASE-04-mercadona-scraper.md))

| API field | Domain model |
|---|---|
| `id` | `ExternalProductId` |
| `display_name` | `ProductName` |
| `details.legal_name` | `LegalName` |
| `details.description` | `ProductDescription` |
| `brand` / `details.brand` | `Brand` |
| `ean` | `Ean` |
| `origin` | `ProductOrigin` |
| `packaging` | `Packaging` |
| `thumbnail` | `ProductThumbnailUrl` |
| `is_bulk` | `isBulk` |
| `is_variable_weight` | `isVariableWeight` |
| `published` | `isActive` |
| `limit` | `purchaseLimit` |
| `details.storage_instructions` | `StorageInstructions` |
| `details.usage_instructions` | `UsageInstructions` |
| `details.mandatory_mentions` | `MandatoryMentions` |
| `details.production_variant` | `ProductionVariant` |
| `details.danger_mentions` | `DangerMentions` |
| `details.suppliers[].name` | `List<Supplier>` |
| `nutrition_information.allergens` | `Allergens` |
| `nutrition_information.ingredients` | `Ingredients` |
| `badges.is_water` | `ProductBadges.isWater` |
| `badges.requires_age_check` | `ProductBadges.requiresAgeCheck` |
| `price_instructions` | `PriceInstructions` (Value Object) |

See also: [Database Schema](APPENDIX.md#database-schema-overview)

---

### Step 31 ⬜ — Define Product core Value Objects
- Create `product/domain/model/ProductId.java` (record, UUID)
- Create `product/domain/model/ExternalProductId.java` (record, non-blank String — Mercadona uses `"3400"`, other supermarkets may use different formats)
- Create `product/domain/model/ProductName.java` (record, non-blank, max 255 — maps to `display_name`)
- Create `product/domain/model/LegalName.java` (record, nullable, max 255 — maps to `details.legal_name`)
- Create `product/domain/model/ProductDescription.java` (record, nullable, max 2000 — maps to `details.description`)
- Create `product/domain/model/Brand.java` (record, nullable, max 255)
- Create `product/domain/model/Ean.java` (record, nullable, max 30 — EAN-13/EAN-8 barcode)
- Create `product/domain/model/ProductOrigin.java` (record, nullable, max 500 — maps to `origin`)
- Create `product/domain/model/Packaging.java` (record, nullable, max 100 — maps to `packaging`, e.g. "Bandeja")
- Create `product/domain/model/ProductThumbnailUrl.java` (record, nullable, max 1000)
- **Verify:** unit tests on each Value Object constructor — null/blank/length invariants

### Step 32 ⬜ — Define Product detail Value Objects
- Create `product/domain/model/StorageInstructions.java` (record, nullable, max 500)
- Create `product/domain/model/UsageInstructions.java` (record, nullable, max 500)
- Create `product/domain/model/MandatoryMentions.java` (record, nullable, max 1000)
- Create `product/domain/model/ProductionVariant.java` (record, nullable, max 500 — maps to `details.production_variant`, e.g. "La unidad puede pesar entre 0,450-0,750 kg.")
- Create `product/domain/model/DangerMentions.java` (record, nullable, max 1000)
- Create `product/domain/model/Allergens.java` (record, nullable, max 2000 — may contain HTML from Mercadona API)
- Create `product/domain/model/Ingredients.java` (record, nullable, max 2000)
- Create `product/domain/model/Supplier.java` (record — `name` non-blank)
- Create `product/domain/model/ProductBadges.java` (record — `boolean isWater`, `boolean requiresAgeCheck`)
- **Verify:** unit tests on constructors; `ProductBadges` — both `false` by default in `create()`

### Step 33 ⬜ — Define Price Value Objects
- Create `shared/domain/model/Money.java` (record — `BigDecimal amount`, `String currency` ISO-4217, amount non-negative)
  - `add(Money other)` — throws `BusinessRuleViolationException` on currency mismatch
- Create `product/domain/model/PriceInstructions.java` (record — the entire `price_instructions` block):
  - `Money unitPrice` — maps to `unit_price` (e.g. `"4.84"` EUR)
  - `Money bulkPrice` — maps to `bulk_price` (e.g. `"8.20"` EUR), nullable
  - `Money referencePrice` — maps to `reference_price`, nullable
  - `String referenceFormat` — maps to `reference_format` (e.g. `"kg"`), nullable
  - `String sizeFormat` — maps to `size_format` (e.g. `"kg"`), nullable
  - `Double unitSize` — maps to `unit_size` (e.g. `0.59`), nullable (approx weight/volume)
  - `Integer totalUnits` — maps to `total_units`, nullable
  - `Integer iva` — VAT percentage (e.g. `10`), **nullable** (null for most fresh produce)
  - `String taxPercentage` — maps to `tax_percentage` (e.g. `"10.000"`)
  - `SellingMethod sellingMethod` — enum wrapping `selling_method` integer
  - `boolean isNew`, `boolean isPack`, `boolean approxSize`, `boolean priceDecreased`
  - `boolean unitSelector`, `boolean bunchSelector`
  - `Money previousUnitPrice` — nullable, populated when `price_decreased = true`
  - `Double minBunchAmount`, `Double incrementBunchAmount` — for variable-weight products
  - `Integer packSize` — nullable
  - `String unitName` — nullable (e.g. `"kg"`, `"L"`, `"ud"`)
- Create `product/domain/model/SellingMethod.java` (enum: `UNIT(0)`, `WEIGHT(2)` — maps to `selling_method` integer from API; `fromCode(int)` factory)
- **Verify:** unit tests on `Money` — add, currency mismatch, negative; `SellingMethod.fromCode()` — valid + unknown code

### Step 34 ⬜ — Define Product Aggregate Root
- Create `product/domain/model/Product.java` (Aggregate Root)
  - Identity: `ProductId id`, `ExternalProductId externalId`, `SupermarketId supermarketId`
  - Core: `ProductName name`, `LegalName legalName`, `ProductDescription description`, `Brand brand`, `Ean ean`
  - Detail: `ProductOrigin origin`, `Packaging packaging`, `ProductThumbnailUrl thumbnailUrl`
  - Detail: `StorageInstructions storageInstructions`, `UsageInstructions usageInstructions`
  - Detail: `MandatoryMentions mandatoryMentions`, `ProductionVariant productionVariant`, `DangerMentions dangerMentions`
  - Detail: `Allergens allergens`, `Ingredients ingredients`
  - Detail: `List<Supplier> suppliers` (unmodifiable)
  - Flags: `ProductBadges badges`, `boolean isBulk`, `boolean isVariableWeight`, `boolean isActive`
  - Limit: `int purchaseLimit` (maps to `limit`, default 999)
  - Navigation: `CategoryId categoryId`
  - `create(...)` static factory — sets `isActive = true`, emits `ProductSynced`
  - `update(UpsertProductCommand)` — compares field by field, updates only changed fields, emits `ProductSynced` only if anything changed
  - `deactivate()` — sets `isActive = false`, emits `ProductDeactivated`
  - `pullDomainEvents()` — returns copy and clears list
- **Verify:** unit tests:
  - `create()` — happy path, null `externalId` throws, null `name` throws
  - `update()` — changed fields updated, event emitted; unchanged → no event
  - `deactivate()` — already inactive is a no-op (no event emitted twice)

### Step 35 ⬜ — Product domain events + exceptions
- Create `product/domain/event/ProductEvent.java` (sealed interface — `permits ProductSynced, ProductDeactivated`)
- Create `product/domain/event/ProductSynced.java` (record — `ProductId productId`, `ExternalProductId externalId`, `SupermarketId supermarketId`, `Instant occurredOn`)
- Create `product/domain/event/ProductDeactivated.java` (record — `ProductId productId`, `SupermarketId supermarketId`, `Instant occurredOn`)
- Create `product/domain/exception/ProductNotFoundException.java` (extends `NotFoundException`)
- Create `product/domain/exception/DuplicateProductException.java` (extends `ConflictException`)
- **Verify:** unit tests asserting correct parent class and message format

### Step 36 ⬜ — Define ProductPrice Aggregate (price history)
- Create `product/domain/model/ProductPriceId.java` (record, UUID)
- Create `product/domain/model/ProductPrice.java` (Aggregate Root — immutable snapshot of prices at a given point in time):
  - `ProductPriceId id`
  - `ProductId productId`
  - `PriceInstructions priceInstructions` — the full price block from the API
  - `Instant recordedAt` — set by `create()` to `Instant.now()`
  - `create(ProductId, PriceInstructions)` static factory — emits `ProductPriceRecorded`
  - `pullDomainEvents()`
- Design rationale: storing the entire `PriceInstructions` as a record preserves the complete price context (unit price, bulk price, VAT, selling method, previous price) for historical analysis
- **Verify:** unit tests — happy path, null `productId` throws, null `priceInstructions` throws

### Step 37 ⬜ — ProductPrice domain events + exceptions
- Create `product/domain/event/ProductPriceEvent.java` (sealed interface — `permits ProductPriceRecorded`)
- Create `product/domain/event/ProductPriceRecorded.java` (record — `ProductPriceId id`, `ProductId productId`, `Money unitPrice`, `Money bulkPrice` nullable, `Instant recordedAt`)
- Create `product/domain/exception/ProductPriceNotFoundException.java` (extends `NotFoundException`)
- **Verify:** unit test asserting `ProductPrice.create()` emits `ProductPriceRecorded` with correct prices extracted from `PriceInstructions`

### Step 38 ⬜ — Product + ProductPrice output ports + DTOs
- Create `product/application/port/output/ProductRepositoryPort.java`
  - `save(Product)`, `findById(ProductId)`, `findByExternalIdAndSupermarket(ExternalProductId, SupermarketId)`, `findActiveExternalIdsBySupermarket(SupermarketId)`, `deleteById(ProductId)`
- Create `product/application/port/output/ProductQueryPort.java`
  - `findDetailById(ProductId)`, `findSummariesByCategory(CategoryId, Pageable)`, `findSummariesBySupermarket(SupermarketId, Pageable)`
- Create `product/application/port/output/ProductPriceRepositoryPort.java`
  - `save(ProductPrice)`, `findLatestByProductId(ProductId)`
- Create `product/application/port/output/ProductPriceQueryPort.java`
  - `findHistoryByProductId(ProductId, Pageable)`
- Create DTOs (all records):
  - `product/application/dto/ProductDetailView.java` — all product fields + `ProductPriceView latestPrice`
  - `product/application/dto/ProductSummaryView.java` — `id`, `externalId`, `name`, `brand`, `thumbnailUrl`, `unitPrice`, `bulkPrice`, `isActive`, `supermarketId`, `categoryId`
  - `product/application/dto/ProductPriceView.java` — `id`, `productId`, `unitPrice`, `bulkPrice`, `referencePrice`, `referenceFormat`, `iva`, `priceDecreased`, `previousUnitPrice`, `recordedAt`
  - `product/application/dto/SupplierView.java` (record — `name`)
- **Verify:** compile only

### Step 39 ⬜ — Upsert Product use case (command)
- Create `product/application/port/input/command/UpsertProductUseCase.java`
- Create `product/application/dto/UpsertProductCommand.java` (record — mirrors all `Product` fields + `PriceInstructions priceInstructions`; this is populated by the scraper from the API JSON):
  ```
  externalId, supermarketId, categoryId, name, legalName, description, brand, ean,
  origin, packaging, thumbnailUrl, storageInstructions, usageInstructions,
  mandatoryMentions, productionVariant, dangerMentions, allergens, ingredients,
  suppliers, badges, isBulk, isVariableWeight, purchaseLimit, priceInstructions
  ```
- Create `product/application/command/UpsertProductHandler.java`:
  - If product does not exist → `Product.create(...)` → save → record price
  - If product exists → `product.update(command)` → save only if event was emitted → always record price (daily snapshot)
  - Emits `ProductSynced` if created/updated; always calls `RecordProductPriceUseCase`
- **Verify:** unit tests:
  - New product: saved + price recorded + `ProductSynced` emitted
  - Existing product, changed name: saved + price recorded + `ProductSynced` emitted
  - Existing product, nothing changed: not saved + price recorded + no `ProductSynced`
  - Existing product, price changed: not saved (product unchanged) + price recorded

### Step 40 ⬜ — Deactivate Product use case (command)
- Create `product/application/port/input/command/DeactivateProductUseCase.java`
- Create `product/application/dto/DeactivateProductCommand.java` (record — `ProductId productId`)
- Create `product/application/command/DeactivateProductHandler.java`
  - Loads product by ID → calls `product.deactivate()` → saves → publishes `ProductDeactivated`
- **Verify:** unit tests — happy path + `ProductNotFoundException` when not found + no-op when already inactive (no event published)

### Step 41 ⬜ — Record ProductPrice use case (command)
- Create `product/application/port/input/command/RecordProductPriceUseCase.java`
- Create `product/application/dto/RecordProductPriceCommand.java` (record — `ProductId productId`, `PriceInstructions priceInstructions`)
- Create `product/application/command/RecordProductPriceHandler.java`
  - Always creates a new `ProductPrice` (append-only history — never updates)
  - Saves + publishes `ProductPriceRecorded`
- **Verify:** unit tests — price recorded, event emitted; called twice creates two history records

### Step 42 ⬜ — Get Product + List Products use cases (query)
- Create `product/application/port/input/query/GetProductByIdUseCase.java` + `GetProductByIdQuery.java` + `GetProductByIdHandler.java`
  - Returns `ProductDetailView` with latest price joined
- Create `product/application/port/input/query/ListProductsByCategoryUseCase.java` + `ListProductsByCategoryQuery.java` (record — `CategoryId`, `Pageable`) + `ListProductsByCategoryHandler.java`
- Create `product/application/port/input/query/ListProductsBySupermarketUseCase.java` + `ListProductsBySupermarketQuery.java` + `ListProductsBySupermarketHandler.java`
- **Verify:** unit tests — found, not found (`ProductNotFoundException`), paginated results

### Step 43 ⬜ — Get ProductPrice history use case (query)
- Create `product/application/port/input/query/GetProductPriceHistoryUseCase.java`
- Create `product/application/dto/GetProductPriceHistoryQuery.java` (record — `ProductId`, `Pageable`)
- Create `product/application/query/GetProductPriceHistoryHandler.java`
  - Returns `PageResponse<ProductPriceView>` ordered by `recordedAt DESC`
- **Verify:** unit test with mocked `ProductPriceQueryPort` — pagination respected

### Step 44 ⬜ — Product JPA Entities + Flyway migration
- Create `V5__create_products_table.sql`:
  ```sql
  CREATE TABLE products (
    id                    UUID PRIMARY KEY,
    external_id           VARCHAR(50)  NOT NULL,
    supermarket_id        UUID         NOT NULL REFERENCES supermarkets(id),
    category_id           UUID         NOT NULL REFERENCES categories(id),
    name                  VARCHAR(255) NOT NULL,
    legal_name            VARCHAR(255),
    description           VARCHAR(2000),
    brand                 VARCHAR(255),
    ean                   VARCHAR(30),
    origin                VARCHAR(500),
    packaging             VARCHAR(100),
    thumbnail_url         VARCHAR(1000),
    storage_instructions  VARCHAR(500),
    usage_instructions    VARCHAR(500),
    mandatory_mentions    VARCHAR(1000),
    production_variant    VARCHAR(500),
    danger_mentions       VARCHAR(1000),
    allergens             VARCHAR(2000),
    ingredients           VARCHAR(2000),
    is_water              BOOLEAN      NOT NULL DEFAULT FALSE,
    requires_age_check    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_bulk               BOOLEAN      NOT NULL DEFAULT FALSE,
    is_variable_weight    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    purchase_limit        INTEGER      NOT NULL DEFAULT 999,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT uq_product_external_supermarket UNIQUE (external_id, supermarket_id)
  );
  CREATE TABLE product_suppliers (
    product_id  UUID         NOT NULL REFERENCES products(id),
    name        VARCHAR(255) NOT NULL,
    position    SMALLINT     NOT NULL,
    PRIMARY KEY (product_id, position)
  );
  ```
- Create `V6__create_product_prices_table.sql`:
  ```sql
  CREATE TABLE product_prices (
    id                    UUID        PRIMARY KEY,
    product_id            UUID        NOT NULL REFERENCES products(id),
    unit_price            NUMERIC(10,2) NOT NULL,
    bulk_price            NUMERIC(10,2),
    reference_price       NUMERIC(10,2),
    reference_format      VARCHAR(20),
    size_format           VARCHAR(20),
    unit_size             NUMERIC(8,3),
    unit_name             VARCHAR(20),
    total_units           INTEGER,
    pack_size             INTEGER,
    iva                   INTEGER,
    tax_percentage        VARCHAR(10),
    selling_method        SMALLINT    NOT NULL,
    is_new                BOOLEAN     NOT NULL DEFAULT FALSE,
    is_pack               BOOLEAN     NOT NULL DEFAULT FALSE,
    approx_size           BOOLEAN     NOT NULL DEFAULT FALSE,
    price_decreased       BOOLEAN     NOT NULL DEFAULT FALSE,
    unit_selector         BOOLEAN     NOT NULL DEFAULT FALSE,
    bunch_selector        BOOLEAN     NOT NULL DEFAULT FALSE,
    previous_unit_price   NUMERIC(10,2),
    min_bunch_amount      NUMERIC(8,3),
    increment_bunch_amount NUMERIC(8,3),
    currency              CHAR(3)     NOT NULL DEFAULT 'EUR',
    recorded_at           TIMESTAMPTZ NOT NULL
  );
  CREATE INDEX idx_product_prices_product_recorded ON product_prices (product_id, recorded_at DESC);
  ```
- Create `ProductEntity.java` (with `@ElementCollection` for suppliers list → `product_suppliers`)
- Create `ProductPriceEntity.java`
- **Verify:** Flyway migrations run clean; JPA schema validation passes

### Step 45 ⬜ — Product persistence mapper + Spring repositories + JPA adapters
- Create `SpringProductRepository.java`:
  - `findByExternalIdAndSupermarketId(String externalId, UUID supermarketId)`
  - `findActiveExternalIdsBySupermarketId(UUID supermarketId)` → `List<String>` (used by sync to detect deactivations)
  - Paginated `findByCategoryId` and `findBySupermarketId`
- Create `SpringProductPriceRepository.java`:
  - `findTopByProductIdOrderByRecordedAtDesc(UUID productId)` → latest price
  - `findByProductId(UUID productId, Pageable)` → history
- Create `ProductPersistenceMapper.java` — maps `ProductEntity` ↔ `Product` (including suppliers `@ElementCollection` ↔ `List<Supplier>`)
- Create `ProductPricePersistenceMapper.java` — maps `ProductPriceEntity` ↔ `ProductPrice` (flat columns ↔ `PriceInstructions` record)
- Create `ProductJpaAdapter.java` (implements `ProductRepositoryPort` + `ProductQueryPort`)
  - `findDetailById`: JPQL join with latest price subquery
- Create `ProductPriceJpaAdapter.java` (implements `ProductPriceRepositoryPort` + `ProductPriceQueryPort`)
- **Verify:** `@DataJpaTest` + Testcontainers:
  - Upsert a product, retrieve it, verify all fields including suppliers
  - Record two prices, verify `findTopByProductId` returns the most recent
  - Paginated history returns both records in correct order

### Step 46 ⬜ — Product Config + REST controllers
- Create `ProductConfig.java` (register all use case beans with `TransactionTemplate` wrapping)
- Create `ProductController.java`:
  - `GET /api/v1/products/{id}` → `ProductDetailView` with latest price
  - `GET /api/v1/supermarkets/{supermarketId}/products?page=0&size=20` → `PageResponse<ProductSummaryView>`
  - `GET /api/v1/categories/{categoryId}/products?page=0&size=20` → `PageResponse<ProductSummaryView>`
- Create `ProductPriceController.java`:
  - `GET /api/v1/products/{productId}/prices?page=0&size=20` → `PageResponse<ProductPriceView>` (price history)
- Create REST DTOs (separate from application DTOs):
  - `ProductDetailResponse.java`, `ProductSummaryResponse.java`, `ProductPriceResponse.java`
  - All with `@Schema` annotations for OpenAPI
- **Verify:** `@WebMvcTest` — GET by id (found + 404), paginated list returns correct structure

