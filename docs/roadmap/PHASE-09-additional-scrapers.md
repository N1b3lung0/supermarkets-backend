# Phase 9 — Additional Scrapers

> **Steps 74–80** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

> Each supermarket follows the same pattern as Mercadona (Phase 4):
> research API → save fixtures → implement adapters → integrate into scheduler.

### Step 74 ⬜ — Research Carrefour API
- Investigate `carrefour.es` internal API structure (categories + products endpoints)
- Document findings in `docs/scrapers/carrefour-api.md`
- Save fixture JSONs in `src/test/resources/fixtures/carrefour/`
- **Verify:** documentation + fixtures exist; API structure understood

### Step 75 ⬜ — Implement CarrefourCategoryScraperAdapter + CarrefourProductScraperAdapter
- Follow same hexagonal pattern as Mercadona (Steps 51–52)
- Add `CarrefourScraperConfig.java` + `CarrefourScraperProperties`
- Add `app.scraper.carrefour.base-url` to `application.yaml`
- **Verify:** unit tests with Carrefour fixture JSONs

### Step 76 ⬜ — Integrate Carrefour into DailySyncScheduler
- Add Carrefour supermarket id to the scheduler loop (already seeded in V3)
- **Verify:** manual trigger `POST /api/v1/sync/supermarkets/{carrefourId}` completes without error; SyncRun shows COMPLETED

### Step 77 ⬜ — Research ALDI API + implement adapters
- Same pattern: research → `docs/scrapers/aldi-api.md` → fixtures → `AldiCategoryScraperAdapter` + `AldiProductScraperAdapter` → config → integration
- **Verify:** unit tests + manual trigger

### Step 78 ⬜ — Research LIDL API + implement adapters
- Same pattern
- **Verify:** unit tests + manual trigger

### Step 79 ⬜ — Research Alcampo API + implement adapters
- Same pattern
- **Verify:** unit tests + manual trigger

### Step 80 ⬜ — Research DIA API + implement adapters
- Same pattern
- **Verify:** unit tests + manual trigger

