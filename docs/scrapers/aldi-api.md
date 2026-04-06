# ALDI España API Research

> Researched: 2026-04-06

## Summary

ALDI España (`www.aldi.es`) exposes a **REST JSON API** used by its single-page product catalog.
The API is **not authenticated**, but requests from non-browser clients are blocked by Cloudflare
(HTTP 403) in the same way as Carrefour. Fixture-based unit tests are used during development.

## API Endpoints Investigated

| Endpoint | HTTP status | Notes |
|----------|-------------|-------|
| `GET /api/front/v1/categories?lang=es` | 403 | Cloudflare block |
| `GET /api/front/v1/products?categoryId={id}&lang=es&page=0&size=48` | 403 | Cloudflare block |

## Known API Shape (from browser-based research)

ALDI España's internal API follows a REST + pagination pattern.

### Categories

```
GET /api/front/v1/categories?lang=es
```

Returns a flat list of top-level categories. Each category may contain sub-categories:

```json
[
  {
    "id": "frescos",
    "name": "Frescos",
    "imageUrl": "https://www.aldi.es/images/cat-frescos.jpg",
    "subcategories": [
      {
        "id": "frutas-y-verduras",
        "name": "Frutas y verduras",
        "imageUrl": "https://www.aldi.es/images/cat-frutas.jpg"
      }
    ]
  }
]
```

### Products by category

```
GET /api/front/v1/products?categoryId={subcategoryId}&lang=es&page=0&size=48
```

Response:

```json
{
  "content": [
    {
      "id": "24356",
      "ean": "4056489956037",
      "name": "Leche entera ALDI 1 l",
      "brand": "ALDI",
      "price": 0.65,
      "pricePerUnit": 0.65,
      "unitLabel": "l",
      "currency": "EUR",
      "imageUrl": "https://www.aldi.es/images/products/24356.jpg",
      "categoryId": "lacteos",
      "available": true
    }
  ],
  "totalElements": 120,
  "totalPages": 3,
  "number": 0,
  "size": 48
}
```

## Access Strategy

Same as Carrefour:
1. **Fixture-based unit tests** — hand-crafted JSON matching the known API shape.
2. **`ExternalServiceException`** — thrown at runtime when Cloudflare blocks (HTTP 403),
   allowing the `SyncRun` to record `FAILED` gracefully.
3. **Future work** — investigate Playwright/Puppeteer headless browser or official ALDI
   data feed for production use.

## Pagination

Products endpoint uses Spring-style pagination:
- `page` — 0-based page index
- `size` — page size (default 48)
- `totalPages` — total number of pages

## Category structure

ALDI España uses a 2-level category structure:
- **TOP** — top-level category (e.g. "Frescos", "Bebidas")
- **SUB** — sub-category leaf used for product fetching (e.g. "Frutas y verduras", "Lácteos")

