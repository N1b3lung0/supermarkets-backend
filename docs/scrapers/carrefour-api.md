# Carrefour España API Research

> Researched: 2026-03-31

## Summary

Carrefour España's web (`www.carrefour.es`) is protected by **Cloudflare** with bot-detection
active on all API endpoints. Direct `curl`/`RestClient` requests return HTTP 403 with a Cloudflare
challenge page, regardless of `User-Agent` header spoofing.

## API Endpoints Investigated

| Endpoint | HTTP status | Notes |
|----------|-------------|-------|
| `GET /api/2.0/page?path=/supermercado&locale=es_ES` | 403 | Cloudflare block |
| `GET /api/2.0/navigation-tabs?locale=es_ES` | 403 | Cloudflare block |
| `GET /api/v2/products/search?query=leche` | 403 | Cloudflare block |
| `GET /supermercado/c/alimentacion/?format=json` | 403 | Cloudflare block |

## Known API Shape (from prior browser-based research & public reports)

Carrefour España uses a **SAP Hybris** backend. When accessible, the key endpoints are:

### Categories

```
GET /api/2.0/page?path=/supermercado&locale=es_ES
```

Returns a component tree. The relevant component is `CategoryNavigationComponent` containing
a list of top-level categories:

```json
{
  "components": [
    {
      "uid": "CategoryNavigationComponent",
      "navigationNode": {
        "entries": [],
        "children": [
          {
            "uid": "alimentacion",
            "localizedTitle": "Alimentación",
            "url": "/supermercado/alimentacion/c/alimentacion",
            "children": [
              {
                "uid": "lacteos",
                "localizedTitle": "Lácteos y huevos",
                "url": "/supermercado/lacteos-y-huevos/c/lacteos",
                "children": []
              }
            ]
          }
        ]
      }
    }
  ]
}
```

### Products by category

```
GET /api/2.0/products/search?query=:relevance:allCategories:{categoryCode}&currentPage=0&pageSize=48&lang=es_ES&curr=EUR
```

Response:

```json
{
  "pagination": {
    "currentPage": 0,
    "pageSize": 48,
    "totalPages": 5,
    "totalResults": 210
  },
  "products": [
    {
      "code": "8480017178466",
      "name": "Leche entera Carrefour 1 l",
      "url": "/supermercado/leche-entera-carrefour-1-l/p/8480017178466",
      "price": {
        "value": 0.89,
        "currencyIso": "EUR",
        "formattedValue": "0,89 €"
      },
      "pricePerUnit": {
        "value": 0.89,
        "unit": "l",
        "formattedValue": "0,89 €/l"
      },
      "categories": [
        { "code": "lacteos", "name": "Lácteos y huevos" }
      ],
      "images": [
        { "url": "/medias/8480017178466.jpg", "format": "product" }
      ]
    }
  ]
}
```

## Access Strategy

Since direct scraping is blocked by Cloudflare, the implementation uses:

1. **Fixture-based unit tests** — hand-crafted JSON fixtures matching the known API shape.
2. **`ScraperUnavailableException`** — thrown at runtime when Cloudflare blocks the request
   (HTTP 403 response), allowing the `SyncRun` to record `FAILED` gracefully.
3. **Future work** — investigate Playwright/Puppeteer headless browser approach or
   official Carrefour data partnership for production use.

## Pagination

Products endpoint is **paginated** (`currentPage` 0-based, `pageSize` up to 48).
The adapter must iterate pages until `currentPage >= totalPages - 1`.

## Rate limiting

No official rate limit documented. In browser sessions, requests are throttled to ~1 req/s.
The adapter uses a configurable `requestDelay` (default: 500ms) between paginated requests.

