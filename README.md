# Supermarkets Price Comparator

Backend API for comparing product prices across the main Spanish supermarkets:
**Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA**.

Built with Java 25 + Spring Boot 4, applying Hexagonal Architecture, DDD, and CQRS.

---

## Quick Start

### Prerequisites

- Java 25 (Amazon Corretto recommended)
- Docker + Docker Compose
- Gradle 9 (wrapper included)

### Run locally

```bash
# 1. Copy environment variables
cp .env.example .env

# 2. Start PostgreSQL
docker compose up -d

# 3. Run the application
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
Health check: `http://localhost:8080/actuator/health`

---

## Code Quality

| Tool | Command | Purpose |
|---|---|---|
| Spotless | `./gradlew spotlessApply` | Auto-format (google-java-format) |
| Spotless | `./gradlew spotlessCheck` | Verify formatting (CI) |
| Checkstyle | `./gradlew checkstyleMain` | Style rules (no star imports, 120-char limit) |
| ArchUnit | `./gradlew test --tests "*ArchitectureTest"` | Architecture boundary rules |

### Install pre-commit hook

Automatically runs Spotless + Checkstyle before every `git commit`:

```bash
chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh
```

---

## Project Structure

See [`CLAUDE.md`](CLAUDE.md) for full architecture conventions and coding guidelines.  
See [`ROADMAP.md`](ROADMAP.md) for the full development roadmap and progress tracking.

---

## Tech Stack

| Component | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.x |
| PostgreSQL | 17 |
| Flyway | 11.x |
| MapStruct | 1.6.x |
| springdoc-openapi | 2.x |

