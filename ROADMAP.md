# ROADMAP — Supermarkets Price Comparator Backend

## Context & Architecture

**Java 25 / Spring Boot 4** backend applying **Hexagonal Architecture + DDD + CQRS**.
Goal: compare product prices across Spanish supermarkets (Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA).
Starting with Mercadona via its internal API (`tienda.mercadona.es/api`).

All conventions follow [`CLAUDE.md`](CLAUDE.md). Every step produces **working, testable code**.

---

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

## Phase Index

| Phase | File | Steps | Status |
|---|---|---|---|
| **Phase 0** — Project Foundation | [PHASE-00-foundation.md](docs/roadmap/PHASE-00-foundation.md) | 1–10 | ✅ |
| **Phase 1** — Supermarket BC | [PHASE-01-supermarket.md](docs/roadmap/PHASE-01-supermarket.md) | 11–22 | ✅ |
| **Phase 2** — Category BC | [PHASE-02-category.md](docs/roadmap/PHASE-02-category.md) | 23–30 | ✅ |
| **Phase 3** — Product BC | [PHASE-03-product.md](docs/roadmap/PHASE-03-product.md) | 31–46 | ✅ |
| **Phase 4** — Mercadona Scraper | [PHASE-04-mercadona-scraper.md](docs/roadmap/PHASE-04-mercadona-scraper.md) | 47–52b | ✅ |
| **Phase 5** — Sync Orchestration | [PHASE-05-sync.md](docs/roadmap/PHASE-05-sync.md) | 53–59 | ✅ |
| **Phase 6** — Scheduler (Daily Sync) | [PHASE-06-scheduler.md](docs/roadmap/PHASE-06-scheduler.md) | 60–61 | ✅ |
| **Phase 7** — Comparison BC | [PHASE-07-comparison.md](docs/roadmap/PHASE-07-comparison.md) | 62–66 | ✅ |
| **Phase 8** — Basket BC | [PHASE-08-basket.md](docs/roadmap/PHASE-08-basket.md) | 67–73 | ✅ |
| **Phase 9** — Additional Scrapers | [PHASE-09-additional-scrapers.md](docs/roadmap/PHASE-09-additional-scrapers.md) | 74–80 | 🔄 |
| **Phase 10** — Performance & Data | [PHASE-10-performance.md](docs/roadmap/PHASE-10-performance.md) | 81–86 | ⬜ |
| **Phase 11** — Security | [PHASE-11-security.md](docs/roadmap/PHASE-11-security.md) | 87–88 | ⬜ |
| **Phase 12** — Observability | [PHASE-12-observability.md](docs/roadmap/PHASE-12-observability.md) | 89–91 | ⬜ |
| **Phase 13** — CI/CD Pipeline | [PHASE-13-cicd.md](docs/roadmap/PHASE-13-cicd.md) | 92–96 | ⬜ |
| **Phase 14** — Polish & Docs | [PHASE-14-polish.md](docs/roadmap/PHASE-14-polish.md) | 97–101 | ⬜ |

**Total: 101 steps** (+ 1 optional: Step 52b)

---

## Reference Material

| Document | Contents |
|---|---|
| [APPENDIX.md](docs/roadmap/APPENDIX.md) | Bounded Context Map, DB Schema, Mercadona API examples, SellingMethod mapping, Category level mapping, Flyway migration order |

---

## Quick Step Lookup

<details>
<summary>All steps at a glance</summary>

### Phase 0 — Foundation (Steps 1–10)
1. Migrate build to Kotlin DSL + Version Catalog
2. Add Spotless + Checkstyle
3. Configure PostgreSQL via Docker Compose
4. Add Flyway + first migration
5. Create shared domain exception hierarchy
6. Create GlobalExceptionHandler + PageResponse
7. Create ArchUnit architecture tests skeleton
8. Configure OpenAPI / Swagger
9. Configure Observability (Actuator + Micrometer)
10. Add pre-commit hooks script

### Phase 1 — Supermarket BC (Steps 11–22)
11. Define Supermarket domain model
12. Define Supermarket domain events
13. Define Supermarket domain exceptions
14. Define Supermarket output ports
15. Register Supermarket use case (command)
16. Get Supermarket by ID use case (query)
17. List Supermarkets use case (query)
18. Supermarket JPA Entity + Flyway migration
19. Supermarket persistence mapper + Spring repository
20. Supermarket Config (bean registration)
21. Supermarket REST controller
22. Seed initial Supermarket data (Flyway)

### Phase 2 — Category BC (Steps 23–30)
23. Define Category domain model (3-level: TOP / SUBCATEGORY / LEAF)
24. Category domain events + exceptions
25. Category output ports + DTOs
26. Upsert Category use case (command)
27. Get Category + List Categories use cases (query)
28. Category JPA Entity + Flyway migration
29. Category persistence mapper + Spring repository + JPA adapter
30. Category Config + REST controller

### Phase 3 — Product BC (Steps 31–46)
31. Define Product core Value Objects
32. Define Product detail Value Objects
33. Define Price Value Objects (Money, PriceInstructions, SellingMethod)
34. Define Product Aggregate Root
35. Product domain events + exceptions
36. Define ProductPrice Aggregate (price history)
37. ProductPrice domain events + exceptions
38. Product + ProductPrice output ports + DTOs
39. Upsert Product use case (command)
40. Deactivate Product use case (command)
41. Record ProductPrice use case (command)
42. Get Product + List Products use cases (query)
43. Get ProductPrice history use case (query)
44. Product JPA Entities + Flyway migration
45. Product persistence mapper + Spring repositories + JPA adapters
46. Product Config + REST controllers

### Phase 4 — Mercadona Scraper (Steps 47–52b)
47. Create Mercadona fixture JSON files
48. Define Mercadona API response DTOs
49. Define ScraperPort output ports
50. Configure RestClient bean for Mercadona
51. Implement MercadonaCategoryScraperAdapter
52. Implement MercadonaProductScraperAdapter
52b. *(Optional)* Enrich Product via `/api/products/{id}`

### Phase 5 — Sync Orchestration (Steps 53–59)
53. Define SyncSupermarketCatalogUseCase
54. Implement SyncSupermarketCatalogHandler (categories)
55. Extend SyncSupermarketCatalogHandler (products + prices)
56. Create Sync Config + REST trigger endpoint
57. Add SyncRun audit entity
58. Integrate SyncRun into handler
59. SyncRun JPA persistence + query endpoint

### Phase 6 — Scheduler (Steps 60–61)
60. Add Spring Scheduler configuration
61. Make scheduler configurable + add ShedLock

### Phase 7 — Comparison BC (Steps 62–66)
62. Define ProductComparison domain model
63. Define Comparison output ports + DTOs
64. Compare Products by name use case (query)
65. ProductComparison query implementation (SQL + pg_trgm)
66. Comparison Config + REST controller

### Phase 8 — Basket BC (Steps 67–73)
67. Define Basket domain model
68. Basket domain events + exceptions
69. Basket use cases (commands)
70. Basket use cases (queries)
71. Compare Basket use case (query)
72. Basket JPA persistence + Flyway migration
73. Basket Config + REST controller

### Phase 9 — Additional Scrapers (Steps 74–80)
74. Research Carrefour API
75. Implement Carrefour adapters
76. Integrate Carrefour into scheduler
77. ALDI adapters
78. LIDL adapters
79. Alcampo adapters
80. DIA adapters

### Phase 10 — Performance (Steps 81–86)
81. Add Redis cache for comparison queries
82. Add cache eviction on product sync
83. Add database indexes for common queries
84. Partition product_prices table (time-based)
85. Evaluate and document read scaling options
86. Add Testcontainers-based performance smoke test

### Phase 11 — Security (Steps 87–88)
87. Add Spring Security (stateless JWT)
88. Add OWASP Dependency Check to build

### Phase 12 — Observability (Steps 89–91)
89. Add business metrics to key handlers
90. Add Prometheus + Grafana to compose.yaml
91. Add distributed tracing (OpenTelemetry)

### Phase 13 — CI/CD (Steps 92–96)
92. Create GitHub Actions CI workflow
93. Create Dockerfile
94. Create GitHub Actions release workflow
95. Add Dependabot configuration
96. Add PR template + branch protection docs

### Phase 14 — Polish (Steps 97–101)
97. Write README.md
98. Add E2E integration test: full Mercadona sync
99. Add E2E integration test: basket comparison flow
100. Review and complete OpenAPI documentation
101. Final architecture review

</details>
