# Phase 4 — Mercadona Scraper (Adapter)

> **Steps 47–52b** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

## Context — Real scraping strategy

The scraper is an **infrastructure adapter** that calls Mercadona's internal API and translates responses
into Commands. It never touches the domain directly.

**Only ~100 HTTP calls needed to fetch all ~10,000 products — no per-product calls in the main sync:**

1. `GET /api/categories/` → 26 top-level categories, each with level-1 subcategory ids (no products).
2. `GET /api/categories/{level1Id}` → level-1 subcategory detail, which contains **level-2 leaf groups**,
   each with a **complete `products[]` array** (full `price_instructions`, `thumbnail`, `badges`, `packaging`,
   `display_name`, `published`, `limit`). **No separate `/api/products/{id}` call is needed.**

**Key observations from the real API:**
- Level-2 groups (`id=420` "Aceite de oliva") have `layout`, `image`, `subtitle` fields absent from level-1.
- `iva` in `price_instructions` can be `null` (e.g. fresh produce) — map as `Integer` not `int`.
- `previous_unit_price` can contain leading whitespace (e.g. `"       20.85"`) — must `.strip()` before parsing.
- Products from the categories endpoint do **not** have `ean`, `origin`, `details`, `nutrition_information`,
  `is_bulk`, `is_variable_weight` — those only come from `/api/products/{id}`.
  Decision: store what we have; enrich later via optional per-product call (Step 52b).
- `categories[]` on a product only contains the **level-0** ancestor — the leaf `categoryId` is known
  from the context of the API call itself.

See also: [Mercadona API Response Structure](APPENDIX.md#mercadona-api-response-structure)

---

### Step 47 ⬜ — Create Mercadona fixture JSON files
- Save `https://tienda.mercadona.es/api/categories/` response as
  `src/test/resources/fixtures/mercadona/categories.json`
- Save `https://tienda.mercadona.es/api/categories/112` response (the "Aceite, vinagre y sal" level-1
  subcategory with its leaf groups and their products) as
  `src/test/resources/fixtures/mercadona/subcategory_112.json`
- Save `https://tienda.mercadona.es/api/products/3400` response as
  `src/test/resources/fixtures/mercadona/product_3400.json`
  (used only if we add the optional enrichment call in Step 52b)
- **Verify:** all three files exist and are valid JSON parseable by Jackson

### Step 48 ⬜ — Define Mercadona API response DTOs
All DTOs in `mercadona/infrastructure/adapter/output/scraper/dto/`

**From `GET /api/categories/`:**
- `MercadonaCategoriesResponse.java` (record — `int count`, `List<MercadonaTopCategoryDto> results`)
- `MercadonaTopCategoryDto.java` (record — `int id`, `String name`, `int order`, `boolean isExtended`,
  `List<MercadonaLevel1CategoryDto> categories`)
- `MercadonaLevel1CategoryDto.java` (record — `int id`, `String name`, `int order`, `int layout`,
  `boolean published`, `boolean isExtended`)
  — no products at this level

**From `GET /api/categories/{level1Id}`:**
- `MercadonaLevel1DetailDto.java` (record — `int id`, `String name`, `int order`, `int layout`,
  `boolean published`, `boolean isExtended`, `List<MercadonaLeafGroupDto> categories`)
- `MercadonaLeafGroupDto.java` (record — `int id`, `String name`, `int order`, `int layout`,
  `boolean published`, `boolean isExtended`, `String image` nullable, `String subtitle` nullable,
  `List<MercadonaProductInCategoryDto> products`)
- `MercadonaProductInCategoryDto.java` (record — complete product shape as returned inside categories):
  ```
  String id, String slug, int limit, MercadonaBadgesDto badges, String status,
  String packaging, boolean published, String shareUrl, String thumbnail,
  List<MercadonaProductCategoryRefDto> categories,
  String displayName, String unavailableFrom,
  MercadonaPriceInstructionsDto priceInstructions,
  List<String> unavailableWeekdays
  ```
  Note: `ean`, `origin`, `details`, `nutritionInformation`, `isBulk`, `isVariableWeight`
  are **absent** — they only appear in `/api/products/{id}`.

**Shared DTOs:**
- `MercadonaBadgesDto.java` (record — `boolean isWater`, `boolean requiresAgeCheck`)
- `MercadonaProductCategoryRefDto.java` (record — `int id`, `String name`, `int level`, `int order`)
  — only level-0 ancestor is present here
- `MercadonaPriceInstructionsDto.java` (record — all fields nullable where the API sends null):
  ```
  Integer iva,               // nullable — null for fresh produce
  boolean isNew,
  boolean isPack,
  Integer packSize,          // nullable
  String unitName,           // nullable
  Double unitSize,           // e.g. 5.0 for 5L bottle
  String bulkPrice,          // e.g. "3.95"
  String unitPrice,          // e.g. "19.75"
  boolean approxSize,
  String sizeFormat,         // e.g. "l", "kg"
  Integer totalUnits,        // nullable
  boolean unitSelector,
  boolean bunchSelector,
  String drainedWeight,      // nullable
  int sellingMethod,         // 0=UNIT
  String taxPercentage,      // e.g. "4.000"
  boolean priceDecreased,
  String referencePrice,     // e.g. "3.950"
  Double minBunchAmount,
  String referenceFormat,    // e.g. "L", "kg"
  String previousUnitPrice,  // nullable; may have leading whitespace → strip()
  Double incrementBunchAmount
  ```
  All price fields are `String` (decimal strings); parse to `BigDecimal` in mapper, strip whitespace first.

**From `GET /api/products/{id}` (optional enrichment):**
- `MercadonaProductDetailDto.java` (record — full shape including `ean`, `origin`, `details`,
  `isBulk`, `isVariableWeight`, `nutritionInformation`, `photos`)
- `MercadonaDetailsDto.java` (record — `String brand`, `String origin`,
  `List<MercadonaSupplierDto> suppliers`, `String legalName`, `String description`,
  `String dangerMentions`, `String mandatoryMentions`, `String productionVariant`,
  `String usageInstructions`, `String storageInstructions`)
- `MercadonaSupplierDto.java` (record — `String name`)
- `MercadonaNutritionInformationDto.java` (record — `String allergens`, `String ingredients`)
- `MercadonaPhotoDto.java` (record — `String zoom`, `String regular`, `String thumbnail`, `int perspective`)

Use `@JsonProperty` for snake_case → camelCase on all DTOs.

- **Verify:** unit tests deserializing `subcategory_112.json`:
  - Assert 3 leaf groups parsed (ids 420, 422, 421, 424 area)
  - Assert first leaf group has id=420, name="Aceite de oliva", 11 products
  - Assert product id="4241", `unitPrice`="19.75", `bulkPrice`="3.95", `unitSize`=5.0
  - Assert `iva` is `null` on product "4241" (fresh produce, no VAT field)
  - Assert `previousUnitPrice` on product "4641" strips to "20.85" after `.strip()`
- **Verify:** unit test deserializing `product_3400.json` into `MercadonaProductDetailDto`:
  - Assert `isBulk=true`, `isVariableWeight=true`, 8 suppliers, `ean="2105100034004"`

### Step 49 ⬜ — Define ScraperPort output ports
- Create `mercadona/application/port/output/scraper/CategoryScraperPort.java`:
  ```java
  // Returns all 3 levels of UpsertCategoryCommand (TOP, SUBCATEGORY, LEAF)
  List<UpsertCategoryCommand> fetchCategories(SupermarketId supermarketId);
  ```
- Create `mercadona/application/port/output/scraper/ProductScraperPort.java`:
  ```java
  // Fetches all products for a given level-1 subcategory (calls /api/categories/{level1Id})
  // Returns one UpsertProductCommand per product found across all leaf groups
  List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId level1ExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex);
  ```
  The `leafCategoryIndex` maps external leaf-group ids to internal `CategoryId` UUIDs — built by the
  sync handler after categories are persisted, so the scraper can set the correct `categoryId` on each product.
- **Verify:** compile only

### Step 50 ⬜ — Configure RestClient bean for Mercadona
- Create `mercadona/infrastructure/config/MercadonaScraperConfig.java`:
  - `@Bean RestClient mercadonaRestClient(MercadonaScraperProperties props)` with:
    - `baseUrl(props.baseUrl())`
    - `defaultHeader("User-Agent", "Mozilla/5.0")`
    - `defaultHeader("Accept", "application/json")`
- Create `mercadona/infrastructure/config/properties/MercadonaScraperProperties.java`
  (`@ConfigurationProperties(prefix = "app.scraper.mercadona")`) — `String baseUrl`, `Duration requestTimeout`
- Add to `application.yaml`:
  ```yaml
  app:
    scraper:
      mercadona:
        base-url: https://tienda.mercadona.es/api
        request-timeout: 10s
  ```
- **Verify:** `./gradlew bootRun` starts; bean wired correctly

### Step 51 ⬜ — Implement MercadonaCategoryScraperAdapter
- Create `mercadona/infrastructure/adapter/output/scraper/MercadonaCategoryScraperAdapter.java`
  implements `CategoryScraperPort`
  - `GET /categories/` → deserialize `MercadonaCategoriesResponse`
  - For each `MercadonaTopCategoryDto` → emit `UpsertCategoryCommand(level=TOP, parentExternalId=null)`
  - For each `MercadonaLevel1CategoryDto` inside → emit `UpsertCategoryCommand(level=SUBCATEGORY, parentExternalId=topCategory.id)`
  - For each level-1 id → `GET /categories/{level1Id}` → for each `MercadonaLeafGroupDto`
    → emit `UpsertCategoryCommand(level=LEAF, parentExternalId=level1Category.id)`
  - Total commands = 26 TOP + ~100 SUBCATEGORY + ~400 LEAF (approximate)
- **Verify:** unit test with `MockRestServiceServer`:
  - Stub `GET /categories/` with `categories.json`
  - Stub `GET /categories/112` with `subcategory_112.json`
  - Assert TOP command for id=12 "Aceite, especias y salsas" has `parentExternalId=null`, `level=TOP`
  - Assert SUBCATEGORY command for id=112 has `parentExternalId="12"`, `level=SUBCATEGORY`
  - Assert LEAF commands for ids 420, 422, 421, 424 have `parentExternalId="112"`, `level=LEAF`

### Step 52 ⬜ — Implement MercadonaProductScraperAdapter
- Create `mercadona/infrastructure/adapter/output/scraper/MercadonaProductScraperAdapter.java`
  implements `ProductScraperPort`
  - `GET /categories/{level1Id}` → deserialize `MercadonaLevel1DetailDto`
  - For each `MercadonaLeafGroupDto` → for each `MercadonaProductInCategoryDto`:
    - Resolve `categoryId` from `leafCategoryIndex.get(ExternalCategoryId.of(leafGroup.id()))`
    - Map to `UpsertProductCommand`:
      - `externalId` ← `product.id()`
      - `name` ← `product.displayName()`
      - `packaging` ← `product.packaging()`
      - `thumbnailUrl` ← `product.thumbnail()`
      - `isActive` ← `product.published()`
      - `purchaseLimit` ← `product.limit()`
      - `badges` ← `ProductBadges(product.badges().isWater(), product.badges().requiresAgeCheck())`
      - `categoryId` ← resolved from `leafCategoryIndex`
      - `priceInstructions` ← map `MercadonaPriceInstructionsDto`:
        - Parse all String price fields to `BigDecimal` via `new BigDecimal(value.strip())`
        - `iva` may be null → store as `null`
        - `previousUnitPrice` may have leading spaces → `.strip()` before parsing
        - `sellingMethod` ← `SellingMethod.fromCode(dto.sellingMethod())`
      - Fields absent from category endpoint (`ean`, `legalName`, `description`, `brand`, `origin`,
        `suppliers`, `allergens`, `ingredients`, `isBulk`, `isVariableWeight`, `storageInstructions`,
        `usageInstructions`, `mandatoryMentions`, `productionVariant`, `dangerMentions`) → set to `null`
  - Wrap HTTP failures in `ExternalServiceException`
- **Verify:** unit test with `MockRestServiceServer` stubbing `GET /categories/112` with `subcategory_112.json`:
  - Assert 22 `UpsertProductCommand` objects returned (11 + 7 + ... products across all leaf groups)
  - Assert product "4241": `name="Aceite de oliva 0,4º Hacendado"`, `unitPrice=Money("19.75","EUR")`,
    `bulkPrice=Money("3.95","EUR")`, `categoryId` = UUID from `leafCategoryIndex` for id=420
  - Assert product "4641": `previousUnitPrice=Money("20.85","EUR")` (whitespace stripped correctly)
  - Assert product "4706": `iva=4` (non-null case)
  - Assert product "4241": `iva=null` (null case handled without NPE)
  - Assert `legalName`, `ean`, `isBulk` are all `null` (not available in categories endpoint)

### Step 52b ⬜ — (Optional) Enrich Product via `/api/products/{id}`
- Create `mercadona/infrastructure/adapter/output/scraper/MercadonaProductEnrichmentAdapter.java`
  - `GET /products/{externalId}` → deserialize `MercadonaProductDetailDto`
  - Returns an `EnrichProductCommand` with the fields absent from the categories endpoint:
    `ean`, `legalName`, `description`, `brand`, `origin`, `suppliers`, `allergens`, `ingredients`,
    `isBulk`, `isVariableWeight`, `storageInstructions`, `usageInstructions`,
    `mandatoryMentions`, `productionVariant`, `dangerMentions`
- Create `product/application/port/input/command/EnrichProductUseCase.java` + `EnrichProductCommand.java`
  + `EnrichProductHandler.java` — loads product by `(externalId, supermarketId)`, updates only the
  enrichment fields if changed, emits `ProductSynced` if anything changed
- This step is **optional for the initial sync** — run it as a background job after the main sync completes
  to avoid rate-limiting (one HTTP call per product × ~10,000 products)
- **Verify:** unit test on `EnrichProductHandler` — fields updated, no-op when unchanged

