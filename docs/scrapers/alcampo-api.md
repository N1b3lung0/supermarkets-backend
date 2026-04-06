# Alcampo España API Research

> Researched: 2026-04-06

## Summary

Alcampo España (`www.alcampo.es`) uses **SAP Commerce (Hybris)** as its e-commerce backend,
similar to Carrefour. Direct HTTP requests are blocked by Cloudflare (HTTP 403).

## API Endpoints Investigated

| Endpoint | HTTP status | Notes |
|----------|-------------|-------|
| `GET /api/v2/alcampo/categories?lang=es` | 403 | Cloudflare block |
| `GET /api/v2/alcampo/products/search?query=:relevance:allCategories:{id}&pageSize=48&currentPage=0&lang=es` | 403 | Cloudflare block |

## Known API Shape (from browser-based research)

### Categories

```
GET /api/v2/alcampo/categories?lang=es
```

Returns a category tree:

```json
{
  "subcategories": [
    {
      "id": "alimentacion",
      "name": "Alimentación",
      "subcategories": [
        { "id": "lacteos", "name": "Lácteos y huevos" }
      ]
    }
  ]
}
```

### Products by category

```
GET /api/v2/alcampo/products/search?query=:relevance:allCategories:{id}&pageSize=48&currentPage={page}&lang=es
```

Response:

```json
{
  "pagination": { "currentPage": 0, "pageSize": 48, "totalPages": 3, "totalResults": 130 },
  "products": [
    {
      "code": "3560070462858",
      "name": "Leche entera Auchan 1 l",
      "categories": [{ "code": "lacteos" }],
      "price": { "value": 0.75, "currencyIso": "EUR" },
      "images": [{ "url": "/medias/3560070462858.jpg" }]
    }
  ]
}
```

## Access Strategy

Same as Carrefour — fixture-based tests + `ExternalServiceException` on 403.

## Category structure

- **TOP** — top-level category (e.g. "Alimentación")
- **SUB** — leaf sub-category used for product fetching (e.g. "Lácteos y huevos")

