# LIDL España API Research

> Researched: 2026-04-06

## Summary

LIDL España (`www.lidl.es`) exposes a **REST JSON API** backed by its internal product platform.
Direct HTTP requests from non-browser clients are blocked by Cloudflare (HTTP 403).
Fixture-based unit tests cover the known API shape.

## API Endpoints Investigated

| Endpoint | HTTP status | Notes |
|----------|-------------|-------|
| `GET /api/gridboxes/ES/es?assortments={category}` | 403 | Cloudflare block |
| `GET /api/gridboxes/ES/es?assortments=fresecos` | 403 | Cloudflare block |

## Known API Shape (from browser-based research)

LIDL España uses an "assortments" concept: a category slug maps to a product grid.

### Categories

LIDL España does not expose a dedicated categories endpoint. Categories are derived
from the assortment slugs embedded in the navigation HTML:

```json
[
  { "id": "frescos", "name": "Frescos y congelados" },
  { "id": "charcuteria", "name": "Charcutería y quesos" },
  { "id": "panaderia", "name": "Panadería y pastelería" },
  { "id": "bebidas", "name": "Bebidas" },
  { "id": "drogueria", "name": "Droguería e higiene" }
]
```

The LIDL scraper uses a **hardcoded category list** endpoint (`/api/categories/lidl`) that
returns this well-known set of assortment slugs.

### Products by assortment

```
GET /api/gridboxes/ES/es?assortments={slug}&page={page}&pageSize={size}
```

Response:

```json
{
  "gridBoxes": [
    {
      "id": "4056489123456",
      "fullTitle": "Leche entera MILBONA 1 l",
      "price": {
        "price": 0.72,
        "currency": "EUR"
      },
      "image": "https://www.lidl.es/images/products/4056489123456.jpg",
      "category": "frescos",
      "keyfacts": {
        "brand": "MILBONA",
        "unitPrice": "0,72 €/l"
      }
    }
  ],
  "totalCount": 85,
  "page": 0,
  "pageSize": 48
}
```

## Access Strategy

Same as Carrefour / ALDI:
1. **Fixture-based unit tests** — hand-crafted JSON matching the known API shape.
2. **`ExternalServiceException`** — thrown at runtime when blocked (HTTP 403).
3. **Future work** — headless browser or official LIDL data feed.

## Pagination

- `page` — 0-based
- `pageSize` — default 48
- `totalCount` — total product count (derive `totalPages = ceil(totalCount / pageSize)`)

## Category structure

- **TOP** — hardcoded assortment slugs (no sub-categories in LIDL España API)

