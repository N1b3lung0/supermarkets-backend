# DIA España API Research

> Researched: 2026-04-06

## Summary

DIA España (`www.dia.es`) uses a **REST JSON API** on its online supermarket platform.
Direct HTTP requests are blocked by Cloudflare (HTTP 403). Fixture-based unit tests cover the known API shape.

## API Endpoints Investigated

| Endpoint | HTTP status | Notes |
|----------|-------------|-------|
| `GET /api/catalog/categories?lang=es` | 403 | Cloudflare block |
| `GET /api/catalog/products?categoryId={id}&page={n}&size=48` | 403 | Cloudflare block |

## Known API Shape (from browser-based research)

### Categories

```
GET /api/catalog/categories?lang=es
```

Returns a flat list:

```json
[
  {
    "id": "frescos",
    "name": "Frescos",
    "children": [
      { "id": "lacteos-frescos", "name": "Lácteos" }
    ]
  },
  {
    "id": "bebidas",
    "name": "Bebidas",
    "children": []
  }
]
```

### Products by category

```
GET /api/catalog/products?categoryId={id}&page={n}&size=48
```

Response:

```json
{
  "products": [
    {
      "id": "8480017028469",
      "name": "Leche entera DIA 1 l",
      "brand": "DIA",
      "price": 0.63,
      "currency": "EUR",
      "imageUrl": "https://www.dia.es/images/8480017028469.jpg",
      "categoryId": "lacteos-frescos"
    }
  ],
  "page": 0,
  "size": 48,
  "totalElements": 95,
  "totalPages": 2
}
```

## Access Strategy

Same pattern — fixture-based tests + `ExternalServiceException` on 403.

## Category structure

- **TOP** — top-level (e.g. "Frescos", "Bebidas")
- **SUB** — children (e.g. "Lácteos", "Zumos y néctar")

