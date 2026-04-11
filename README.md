# Supermarkets Price Comparator

Backend API for comparing product prices across the main Spanish supermarkets:
**Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA**.

Built with **Java 25 + Spring Boot 4**, applying **Hexagonal Architecture**, **DDD**, and **CQRS**.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture](#architecture)
3. [Local Setup](#local-setup)
4. [Running Tests](#running-tests)
5. [API Reference](#api-reference)
6. [Observability](#observability)
7. [CI / CD](#ci--cd)
8. [Project Docs](#project-docs)

---

## Tech Stack

| Component              | Technology / Version              |
|------------------------|-----------------------------------|
| Language               | Java 25                           |
| Framework              | Spring Boot 4.0.x                 |
| Architecture           | Hexagonal · DDD · CQRS            |
| Persistence            | Spring Data JPA + PostgreSQL 17   |
| Migrations             | Flyway 11.x                       |
| Cache                  | Spring Cache + Redis 7            |
| Security               | Spring Security (stateless JWT / OAuth2) |
| API Docs               | springdoc-openapi 2.x             |
| Scheduler lock         | ShedLock 7.x                      |
| Object mapping         | MapStruct 1.6.x                   |
| Metrics                | Micrometer (OTLP registry)        |
| Tracing                | OpenTelemetry → Jaeger            |
| Logging                | Logback + Logstash JSON encoder   |
| Build tool             | Gradle 9 (Kotlin DSL + Version Catalog) |
| Code style             | Spotless (google-java-format) + Checkstyle |
| Architecture tests     | ArchUnit 1.4.x                    |
| Integration tests      | Testcontainers 1.21.x             |
| Security scan          | OWASP Dependency Check 12.x       |

---

## Architecture

Feature-first **hexagonal layout**. Every bounded context is a self-contained vertical:

```
com.n1b3lung0.supermarkets.<feature>/
  domain/           ← Pure Java: Aggregates, Value Objects, Domain Events, Exceptions
  application/      ← Use Cases (ports), Command/Query Handlers, DTOs
  infrastructure/   ← REST Controllers, JPA Adapters, Spring Config, Scrapers
```

**Bounded contexts:**

```
supermarket · category · product · basket · comparison · sync · shared
```

**Scraper adapters** (infrastructure-only, no domain):
`mercadona` · `alcampo` · `aldi` · `carrefour` · `dia` · `lidl`

**Dependency direction** (enforced by ArchUnit):

```
infrastructure → application → domain   (never reversed)
```

### Package diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        REST / HTTP                           │
│          (SupermarketController, BasketController, …)        │
├──────────────────────────────────────────────────────────────┤
│                    infrastructure/                            │
│   JPA Adapters · Spring Config · Scrapers · Security         │
├──────────────────────────────────────────────────────────────┤
│                     application/                             │
│       Use Case ports · Handlers · DTOs · Mappers            │
├──────────────────────────────────────────────────────────────┤
│                       domain/                                │
│   Aggregates · Value Objects · Domain Events · Exceptions   │
│         (pure Java — zero framework dependencies)            │
└──────────────────────────────────────────────────────────────┘
```

### Data flow for a sync

```
DailySyncScheduler (03:00 CET)
        │
        ▼
SyncSupermarketCatalogHandler
        │
        ├─► MercadonaCategoryScraperAdapter  ──► tienda.mercadona.es/api
        │         └─► UpsertCategoryHandler
        │
        └─► MercadonaProductScraperAdapter   ──► tienda.mercadona.es/api
                  ├─► UpsertProductHandler
                  └─► RecordProductPriceHandler
                            └─► PartitionMaintenancePort (creates month partition)
                  └─► LatestPricesRefreshPort.refresh()  (refreshes materialized view)
```

---

## Local Setup

### Prerequisites

| Tool               | Version |
|--------------------|---------|
| Java               | 25 (Amazon Corretto or Eclipse Temurin) |
| Docker + Compose   | Docker Desktop ≥ 4.x |
| Gradle             | Wrapper included (`./gradlew`) |

### 1 — Copy environment variables

```bash
cp .env.example .env
```

Defaults work out of the box for local development. Edit `.env` only if you need to change ports or credentials.

### 2 — Start infrastructure services

```bash
docker compose up -d
```

This starts:

| Service         | URL / Port                   | Purpose            |
|-----------------|------------------------------|--------------------|
| PostgreSQL 17   | `localhost:5432`             | Primary database   |
| Redis 7         | `localhost:6379`             | Cache              |
| Prometheus      | `http://localhost:9090`      | Metrics scraping   |
| Grafana         | `http://localhost:3000`      | Metrics dashboards |
| Jaeger          | `http://localhost:16686`     | Distributed traces |

### 3 — Run the application

```bash
./gradlew bootRun
```

| Endpoint     | URL                                         |
|--------------|---------------------------------------------|
| API base     | `http://localhost:8080/api/v1`              |
| Swagger UI   | `http://localhost:8080/swagger-ui.html`     |
| Health check | `http://localhost:8080/actuator/health`     |
| Metrics      | `http://localhost:8080/actuator/prometheus` |

> Flyway applies all migrations automatically on startup. No manual DB setup needed.

---

## Running Tests

```bash
# Full local quality gate (format + style + all tests)
./gradlew spotlessCheck checkstyleMain test

# Auto-fix formatting issues
./gradlew spotlessApply

# Unit & application layer tests only (no Spring context — fast)
./gradlew test --tests "*.domain.*" --tests "*.application.*"

# Architecture boundary tests
./gradlew test --tests "*ArchitectureTest"

# Integration tests (requires Docker for Testcontainers)
./gradlew test --tests "*.infrastructure.*"

# OWASP CVE scan (slow — run on-demand or nightly)
./gradlew dependencyCheckAnalyze
```

### Install pre-commit hook

Runs Spotless + Checkstyle automatically before every `git commit`:

```bash
chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh
```

### Test pyramid

| Layer       | Framework                          | Spring context |
|-------------|------------------------------------|----------------|
| Domain      | JUnit 5 + AssertJ                  | No             |
| Application | JUnit 5 + Mockito                  | No             |
| REST        | `@WebMvcTest`                      | Partial        |
| JPA         | `@DataJpaTest` + Testcontainers    | Partial        |
| E2E         | `@SpringBootTest` + Testcontainers | Full           |

---

## API Reference

All endpoints are documented in **Swagger UI** at `http://localhost:8080/swagger-ui.html`.

### Quick reference

#### Supermarkets

| Method | Path                         | Description                   |
|--------|------------------------------|-------------------------------|
| POST   | `/api/v1/supermarkets`       | Register a supermarket        |
| GET    | `/api/v1/supermarkets/{id}`  | Get supermarket by ID         |
| GET    | `/api/v1/supermarkets`       | List supermarkets (paginated) |

#### Categories

| Method | Path                         | Description                                    |
|--------|------------------------------|------------------------------------------------|
| POST   | `/api/v1/categories`         | Register a category                            |
| GET    | `/api/v1/categories/{id}`    | Get category by ID                             |
| GET    | `/api/v1/categories`         | List categories (filter by supermarket/level)  |

#### Products & Prices

| Method | Path                           | Description                  |
|--------|--------------------------------|------------------------------|
| POST   | `/api/v1/products`             | Register / upsert a product  |
| GET    | `/api/v1/products/{id}`        | Get product by ID            |
| GET    | `/api/v1/products`             | List products (paginated)    |
| GET    | `/api/v1/products/{id}/prices` | Price history (newest first) |

#### Price Comparison

| Method | Path                         | Description                                          |
|--------|------------------------------|------------------------------------------------------|
| GET    | `/api/v1/compare?q={name}`   | Compare prices by product name across supermarkets   |

Optional query param: `supermarkets` (comma-separated UUIDs to filter by chain).

#### Basket

| Method | Path                                  | Description                        |
|--------|---------------------------------------|------------------------------------|
| POST   | `/api/v1/baskets`                     | Create a basket                    |
| GET    | `/api/v1/baskets/{id}`                | Get basket by ID                   |
| POST   | `/api/v1/baskets/{id}/items`          | Add item to basket                 |
| PATCH  | `/api/v1/baskets/{id}/items/{itemId}` | Update item quantity               |
| DELETE | `/api/v1/baskets/{id}/items/{itemId}` | Remove item from basket            |
| DELETE | `/api/v1/baskets/{id}/items`          | Clear all items                    |
| GET    | `/api/v1/baskets/{id}/compare`        | Compare basket total across chains |

#### Catalog Sync

| Method | Path                                         | Description                              |
|--------|----------------------------------------------|------------------------------------------|
| POST   | `/api/v1/sync/supermarkets/{supermarketId}`  | Trigger a full catalog sync              |
| GET    | `/api/v1/sync/runs?supermarketId={id}`       | List sync runs (paginated, newest first) |

> All mutation endpoints (`POST`, `PATCH`, `PUT`) require an `Idempotency-Key` header to prevent duplicate processing.

### Error responses

All errors follow [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457):

| HTTP | Category                          | Example                           |
|------|-----------------------------------|-----------------------------------|
| 404  | `NotFoundException`               | Supermarket / product not found   |
| 409  | `ConflictException`               | Duplicate name or external ID     |
| 422  | `BusinessRuleViolationException`  | Price cannot be negative          |
| 403  | `UnauthorizedException`           | Insufficient role                 |
| 502  | `ExternalServiceException`        | Scraper target unreachable        |

---

## Observability

| Tool       | URL                              | Credentials  |
|------------|----------------------------------|--------------|
| Grafana    | `http://localhost:3000`          | admin / admin|
| Prometheus | `http://localhost:9090`          | —            |
| Jaeger     | `http://localhost:16686`         | —            |
| Actuator   | `http://localhost:8080/actuator` | —            |

**Key business metrics** (Micrometer counters / timers):

| Metric                       | Description                          |
|------------------------------|--------------------------------------|
| `supermarkets.sync.duration` | Full catalog sync duration per chain |
| `products.price.recorded`    | Prices recorded per sync             |
| `products.upserted`          | Products created/updated per sync    |

Tracing: every request carries a `traceId` / `spanId` included in JSON log output and exported to Jaeger via OTLP.

---

## CI / CD

GitHub Actions workflows (`.github/workflows/`):

| Workflow       | Trigger                | Steps                                                         |
|----------------|------------------------|---------------------------------------------------------------|
| `ci.yml`       | Push / PR to `master`  | Spotless · Checkstyle · Unit tests · ArchUnit · Integration   |
| `release.yml`  | Push tag `v*.*.*`      | Full quality gate → Docker build/push to GHCR → GitHub Release|

Docker image is built from the multi-stage `Dockerfile`:
- **Builder**: `eclipse-temurin:25-jdk-alpine`
- **Runtime**: `eclipse-temurin:25-jre-alpine`, non-root user, `HEALTHCHECK` on `/actuator/health`

See [`docs/github-setup.md`](docs/github-setup.md) for branch protection rules and required secrets.

---

## Project Docs

| Document | Purpose |
|---|---|
| [`ROADMAP.md`](ROADMAP.md) | Full development roadmap (101 steps) |
| [`CLAUDE.md`](CLAUDE.md) | Architecture conventions & coding rules |
| [`AGENTS.md`](AGENTS.md) | AI coding agent reference |
| [`docs/claude/`](docs/claude/) | Detailed architecture reference (10 docs) |
| [`docs/roadmap/`](docs/roadmap/) | Phase-by-phase implementation docs |
| [`docs/github-setup.md`](docs/github-setup.md) | GitHub setup guide (secrets, branch protection) |
| [`docs/scrapers/`](docs/scrapers/) | Supermarket API research notes |
