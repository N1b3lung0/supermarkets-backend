# Appendix — Reference Material

> [← Index](../../ROADMAP.md)

---

## Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SUPERMARKETS APP                                 │
│                                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌──────────────┐                    │
│  │ Supermarket │───▶│  Category   │───▶│   Product    │                    │
│  │  (BC)       │    │  (BC)       │    │   (BC)       │                    │
│  └─────────────┘    └─────────────┘    └──────┬───────┘                    │
│         │                  │                  │ prices                      │
│         │ sync             │ sync             │ history                     │
│         ▼                  ▼                  ▼                             │
│  ┌──────────────────────────────────────────────────────┐                  │
│  │                  Sync (BC)                           │                  │
│  │   SyncRun audit + orchestrates scraper adapters      │                  │
│  └──────────────────────────────────────────────────────┘                  │
│         │ external API calls                                                │
│         ▼                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────┐  ┌────────────────────┐ │
│  │  Mercadona  │  │  Carrefour  │  │    ALDI    │  │  LIDL/Alcampo/DIA  │ │
│  │  Adapter    │  │  Adapter    │  │  Adapter   │  │  Adapters          │ │
│  └─────────────┘  └─────────────┘  └────────────┘  └────────────────────┘ │
│                                                                             │
│  ┌─────────────────────────────────────────────────────┐                   │
│  │              Comparison (BC)                        │                   │
│  │  Queries latest price per product/supermarket       │                   │
│  └─────────────────────────────────────────────────────┘                   │
│                            ▲                                                │
│                            │ uses ComparisonQueryPort                       │
│  ┌─────────────────────────────────────────────────────┐                   │
│  │                Basket (BC)                          │                   │
│  │  User shopping list + total cost per supermarket    │                   │
│  └─────────────────────────────────────────────────────┘                   │
│                                                                             │
│  ┌──────────────────────────┐                                               │
│  │       Shared Kernel      │                                               │
│  │  Money, PageResponse,    │                                               │
│  │  Exception hierarchy,    │                                               │
│  │  GlobalExceptionHandler  │                                               │
│  └──────────────────────────┘                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Database Schema Overview

```
supermarkets              categories
─────────────             ─────────────────────
id           UUID PK      id              UUID PK
name         VARCHAR      external_id     VARCHAR(50)
country      VARCHAR      name            VARCHAR(255)
created_at   TIMESTAMPTZ  supermarket_id  UUID FK→supermarkets
updated_at   TIMESTAMPTZ  parent_id       UUID FK→categories (nullable)
deleted_at   TIMESTAMPTZ  level           SMALLINT  -- 0=TOP, 1=SUBCATEGORY, 2=LEAF
                          display_order   INT
                          published       BOOLEAN
                          created_at      TIMESTAMPTZ
                          updated_at      TIMESTAMPTZ
                          deleted_at      TIMESTAMPTZ
                          UNIQUE(external_id, supermarket_id)

products
────────────────────────────────────────────────────────────────────
id                  UUID PK
external_id         VARCHAR(50)      ← Mercadona "3400"
supermarket_id      UUID FK→supermarkets
category_id         UUID FK→categories  (LEAF level only)
name                VARCHAR(255)     ← display_name
legal_name          VARCHAR(255)     ← details.legal_name
description         VARCHAR(2000)    ← details.description
brand               VARCHAR(255)     ← brand / details.brand
ean                 VARCHAR(30)      ← ean (EAN-13)
origin              VARCHAR(500)     ← origin
packaging           VARCHAR(100)     ← packaging ("Bandeja")
thumbnail_url       VARCHAR(1000)    ← thumbnail
storage_instructions VARCHAR(500)   ← details.storage_instructions
usage_instructions  VARCHAR(500)     ← details.usage_instructions
mandatory_mentions  VARCHAR(1000)    ← details.mandatory_mentions
production_variant  VARCHAR(500)     ← details.production_variant
danger_mentions     VARCHAR(1000)    ← details.danger_mentions
allergens           VARCHAR(2000)    ← nutrition_information.allergens
ingredients         VARCHAR(2000)    ← nutrition_information.ingredients
is_water            BOOLEAN          ← badges.is_water
requires_age_check  BOOLEAN          ← badges.requires_age_check
is_bulk             BOOLEAN          ← is_bulk
is_variable_weight  BOOLEAN          ← is_variable_weight
is_active           BOOLEAN          ← published
purchase_limit      INT              ← limit (default 999)
created_at          TIMESTAMPTZ
updated_at          TIMESTAMPTZ
deleted_at          TIMESTAMPTZ
UNIQUE(external_id, supermarket_id)

product_suppliers
──────────────────────────────────
product_id  UUID FK→products PK
position    SMALLINT PK
name        VARCHAR(255)           ← details.suppliers[].name

product_prices  (range-partitioned by recorded_at)
────────────────────────────────────────────────────────────────────
id                    UUID PK
product_id            UUID FK→products
unit_price            NUMERIC(10,2)     ← price_instructions.unit_price
bulk_price            NUMERIC(10,2)     ← price_instructions.bulk_price
reference_price       NUMERIC(10,2)     ← price_instructions.reference_price
reference_format      VARCHAR(20)       ← price_instructions.reference_format ("kg")
size_format           VARCHAR(20)       ← price_instructions.size_format
unit_name             VARCHAR(20)       ← price_instructions.unit_name
unit_size             NUMERIC(8,3)      ← price_instructions.unit_size (0.59)
total_units           INT               ← price_instructions.total_units
pack_size             INT               ← price_instructions.pack_size
iva                   INT               ← price_instructions.iva (nullable)
tax_percentage        VARCHAR(10)       ← price_instructions.tax_percentage
selling_method        SMALLINT          ← price_instructions.selling_method (0=UNIT, 2=WEIGHT)
is_new                BOOLEAN
is_pack               BOOLEAN
approx_size           BOOLEAN
price_decreased       BOOLEAN
unit_selector         BOOLEAN
bunch_selector        BOOLEAN
previous_unit_price   NUMERIC(10,2)     ← price_instructions.previous_unit_price (strip whitespace)
min_bunch_amount      NUMERIC(8,3)
increment_bunch_amount NUMERIC(8,3)
currency              CHAR(3) DEFAULT 'EUR'
recorded_at           TIMESTAMPTZ
INDEX(product_id, recorded_at DESC)

baskets                   basket_items
──────────────────────    ──────────────────────
id           UUID PK      id            UUID PK
name         VARCHAR       basket_id     UUID FK→baskets
created_at   TIMESTAMPTZ  product_name  VARCHAR(255)
updated_at   TIMESTAMPTZ  quantity      INT
deleted_at   TIMESTAMPTZ  created_at    TIMESTAMPTZ
                          updated_at    TIMESTAMPTZ

sync_runs
──────────────────────────────────
id                   UUID PK
supermarket_id       UUID FK→supermarkets
started_at           TIMESTAMPTZ
finished_at          TIMESTAMPTZ (nullable)
status               VARCHAR ('RUNNING','COMPLETED','FAILED')
categories_synced    INT
products_synced      INT
products_deactivated INT
error_message        TEXT (nullable)
```

---

## Mercadona API Response Structure

### Level 0+1: `GET https://tienda.mercadona.es/api/categories/`
```json
{
  "count": 26,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 12,
      "name": "Aceite, especias y salsas",
      "order": 7,
      "is_extended": false,
      "categories": [
        { "id": 112, "name": "Aceite, vinagre y sal",       "order": 7, "layout": 1, "published": true, "is_extended": false },
        { "id": 115, "name": "Especias",                    "order": 7, "layout": 1, "published": true, "is_extended": false },
        { "id": 116, "name": "Mayonesa, ketchup y mostaza", "order": 7, "layout": 1, "published": true, "is_extended": false },
        { "id": 117, "name": "Otras salsas",                "order": 7, "layout": 1, "published": true, "is_extended": false }
      ]
    }
  ]
}
```
> **Level-0** = top category (`id=12`). **Level-1** = subcategory inside `categories[]` (`id=112`).
> No products at either level. Level-1 has `layout` and `published`; level-0 does not.

### Level 2 + Products: `GET https://tienda.mercadona.es/api/categories/112`
```json
{
  "id": 112,
  "name": "Aceite, vinagre y sal",
  "categories": [
    {
      "id": 420,
      "name": "Aceite de oliva",
      "order": 7,
      "layout": 2,
      "published": true,
      "is_extended": false,
      "image": null,
      "subtitle": null,
      "products": [
        {
          "id": "4241",
          "slug": "aceite-oliva-04o-hacendado-garrafa",
          "limit": 999,
          "badges": { "is_water": false, "requires_age_check": false },
          "status": null,
          "packaging": "Garrafa",
          "published": true,
          "thumbnail": "https://prod-mercadona.imgix.net/images/xxx.jpg?fit=crop&h=300&w=300",
          "categories": [
            { "id": 12, "name": "Aceite, especias y salsas", "level": 0, "order": 7 }
          ],
          "display_name": "Aceite de oliva 0,4º Hacendado",
          "unavailable_from": null,
          "price_instructions": {
            "iva": null,
            "unit_price": "19.75",
            "bulk_price": "3.95",
            "unit_size": 5.0,
            "size_format": "l",
            "selling_method": 0,
            "tax_percentage": "4.000",
            "price_decreased": false,
            "reference_price": "3.950",
            "reference_format": "L",
            "previous_unit_price": null
          }
        },
        {
          "id": "4641",
          "display_name": "Aceite de oliva 1º Hacendado",
          "price_instructions": {
            "iva": null,
            "unit_price": "19.75",
            "previous_unit_price": "       20.85"
          }
        }
      ]
    }
  ]
}
```
> **Level-2** = leaf group (`id=420`) — has `image`, `subtitle`, and **`products[]`** with complete price data.
> Products here include full `price_instructions` but **NOT** `ean`, `origin`, `details`,
> `nutrition_information`, `is_bulk`, `is_variable_weight` — those require `GET /api/products/{id}`.
>
> ⚠️ **`iva` can be `null`** — use `Integer` not `int` in the DTO.
> ⚠️ **`previous_unit_price`** can contain leading whitespace — always call `.strip()` before parsing.
> ⚠️ Product's `categories[]` only shows the **level-0 ancestor** — the leaf category id comes from `leafGroup.id()`.

### Optional enrichment: `GET https://tienda.mercadona.es/api/products/3400`
```json
{
  "id": "3400",
  "ean": "2105100034004",
  "is_bulk": true,
  "is_variable_weight": true,
  "details": {
    "legal_name": "Filetes filetados de pechuga de pollo",
    "description": "Filetes pechuga de pollo corte fino mínimo 10 filetes",
    "suppliers": [ { "name": "AVICOLA DE LLEIDA S.A.U." }, { "name": "UVE, S.A." } ],
    "usage_instructions": "Cocinar completamente (75º C) antes de su consumo",
    "storage_instructions": "Conservar entre 0 y 4C. Una vez abierto consumir en 24 horas.",
    "mandatory_mentions": "Producto fresco. Categoría A.",
    "production_variant": "La unidad puede pesar entre 0,450-0,750 kg.",
    "danger_mentions": ""
  },
  "nutrition_information": {
    "allergens": " <strong>x99</strong>.",
    "ingredients": "100% pollo"
  }
}
```
> Only needed for enrichment fields. Use as a background job after the main sync to avoid rate-limiting.

---

## SellingMethod Mapping

| API `selling_method` | Domain `SellingMethod` | Meaning |
|---|---|---|
| `0` | `UNIT` | Sold by unit or fixed-size pack |
| `2` | `WEIGHT` | Sold by weight (variable price; `unit_size` is approx kg) |

> Additional codes may appear for other supermarkets — add to the enum as discovered.

---

## Category Level Mapping

| API level | Domain `CategoryLevel` | Has products | Example |
|---|---|---|---|
| Level-0 (in `results[]`) | `TOP` | No | id=12 "Aceite, especias y salsas" |
| Level-1 (in `results[].categories[]`) | `SUBCATEGORY` | No | id=112 "Aceite, vinagre y sal" |
| Level-2 (in `GET /categories/{level1Id}` → `categories[]`) | `LEAF` | **Yes** | id=420 "Aceite de oliva" |

---

## Flyway Migration Order

| Version | File | Description |
|---|---|---|
| V1 | `V1__init.sql` | No-op baseline |
| V2 | `V2__create_supermarkets_table.sql` | Supermarkets table |
| V3 | `V3__seed_supermarkets.sql` | Seed 6 supermarkets |
| V4 | `V4__create_categories_table.sql` | Categories table (3-level) |
| V5 | `V5__create_products_table.sql` | Products + product_suppliers |
| V6 | `V6__create_product_prices_table.sql` | Price history |
| V7 | `V7__create_sync_runs_table.sql` | Sync audit |
| V8 | `V8__create_shedlock_table.sql` | ShedLock |
| V9 | `V9__add_product_name_search_index.sql` | pg_trgm + GIN index |
| V10 | `V10__create_baskets_table.sql` | Baskets |
| V11 | `V11__create_basket_items_table.sql` | Basket items |
| V12 | `V12__add_performance_indexes.sql` | Additional indexes |
| V13 | `V13__partition_product_prices_by_month.sql` | Time partitioning |
| V14 | `V14__create_latest_product_prices_view.sql` | Materialized view |

