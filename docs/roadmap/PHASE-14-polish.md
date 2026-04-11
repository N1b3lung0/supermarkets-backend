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

### Step 99 ⬜ — Add E2E integration test: basket comparison flow
- Create a basket, add 3 items (matching seeded products from multiple supermarkets)
- Call `GET /api/v1/baskets/{id}/compare`
- Assert cheapest supermarket is identified correctly based on seeded prices
- **Verify:** test passes in CI

### Step 100 ⬜ — Review and complete OpenAPI documentation
- Ensure all controllers have `@Tag`, `@Operation`, `@ApiResponses`
- Ensure all request DTOs have `@Schema` on fields with `description` and `example`
- Ensure `ProblemDetail` error responses are documented
- **Verify:** Swagger UI shows complete, accurate documentation for all endpoints; no undocumented operations

### Step 101 ⬜ — Final architecture review
- Run ArchUnit tests — all green
- Run `./gradlew spotlessCheck checkstyleMain` — all green
- Review package structure against `CLAUDE.md` conventions
- Check `shared/` for anything that belongs in a bounded context
- Verify no circular dependencies between bounded contexts
- **Verify:** `./gradlew build` fully green; no TODO comments left in production code

