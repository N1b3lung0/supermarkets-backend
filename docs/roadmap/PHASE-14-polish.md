# Phase 14 — Polish & Documentation

> **Steps 97–101** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 97 ✅ — Write README.md
- Project overview, tech stack, local setup (Docker Compose), running tests, API endpoints summary, architecture diagram (ASCII or Mermaid)
- **Verify:** a fresh clone can run the app by following README steps alone

### Step 98 ✅ — Add E2E integration test: full Mercadona sync
- `@SpringBootTest` + Testcontainers (PostgreSQL + Redis)
- Trigger `POST /api/v1/sync/supermarkets/{mercadonaId}` with mocked HTTP (`MockRestServiceServer`) using full fixture
- Assert categories count, products count, prices recorded, SyncRun status = `COMPLETED`
- **Verify:** test passes in CI

### Step 99 ✅ — Add E2E integration test: basket comparison flow
- Create a basket, add 3 items (matching seeded products from multiple supermarkets)
- Call `GET /api/v1/baskets/{id}/compare`
- Assert cheapest supermarket is identified correctly based on seeded prices
- **Verify:** test passes in CI

### Step 100 ✅ — Review and complete OpenAPI documentation
- Ensure all controllers have `@Tag`, `@Operation`, `@ApiResponses` — ✅ all 7 controllers fully annotated
- Ensure all request DTOs have `@Schema` on fields with `description` and `example` — ✅ all request records annotated
- Added JWT Bearer security scheme to `OpenApiConfig` — "Authorize" button in Swagger UI
- Added `V15__demo_data.sql` — 16 products across Mercadona, Carrefour, ALDI with realistic prices; `latest_product_prices` refreshed at end of migration
- **Verify:** Swagger UI shows complete documentation; `GET /api/v1/compare?q=leche` returns results without any sync

### Step 101 ✅ — Final architecture review
- ArchUnit tests — all green (`domainIsIsolated`, `applicationDoesNotDependOnInfrastructure`, `noSpringAnnotationsInDomainOrApplication`)
- `./gradlew spotlessCheck checkstyleMain` — all green
- Package structure matches `CLAUDE.md` conventions — all 14 contexts (+ `shared`) under correct feature-first hexagonal layout
- `shared/` review — all contents are genuinely cross-cutting (exception hierarchy, `Money`, `PageResponse`, `GlobalExceptionHandler`, `SecurityConfig`, `CacheConfig`, `OpenApiConfig`)
- Cross-context dependencies verified — no circular dependencies; all cross-context references are either ID Value Objects or application ports (documented in `AGENTS.md`)
- Zero TODO/FIXME/HACK comments in production code
- **Verify:** `./gradlew build` fully green ✅

