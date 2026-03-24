# Phase 13 — CI/CD Pipeline

> **Steps 92–96** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 92 ⬜ — Create GitHub Actions CI workflow
- Create `.github/workflows/ci.yml` triggered on `push` and `pull_request` to `main`/`develop`
- Jobs (in order):
  1. `./gradlew spotlessCheck`
  2. `./gradlew checkstyleMain`
  3. `./gradlew test` (domain + application unit tests — no Testcontainers)
  4. `./gradlew test --tests "*ArchitectureTest"` (ArchUnit)
  5. `./gradlew integrationTest` (Testcontainers — DB + Redis)
- Cache Gradle dependencies between runs
- **Verify:** push to a branch → green CI run on GitHub

### Step 93 ⬜ — Create Dockerfile
- Multi-stage build per `CLAUDE.md` conventions:
  ```dockerfile
  FROM eclipse-temurin:25-jdk-alpine AS builder
  # gradle build
  FROM eclipse-temurin:25-jre-alpine AS runtime
  # copy jar, non-root user, health check
  ```
- Non-root user (`appuser`)
- `HEALTHCHECK` pointing to `/actuator/health`
- JVM flag `-XX:+UseContainerSupport`
- **Verify:** `docker build -t supermarkets:local . && docker run -p 8080:8080 supermarkets:local` starts and health check passes

### Step 94 ⬜ — Create GitHub Actions release workflow
- Create `.github/workflows/release.yml` triggered on push of `v*.*.*` tags
- Steps: full CI → Docker build → push to GHCR (`ghcr.io/{owner}/supermarkets:{tag}`) → GitHub Release with auto-generated notes
- **Verify:** create `v0.1.0` tag → release workflow completes; image visible in GHCR

### Step 95 ⬜ — Add Dependabot configuration
- Create `.github/dependabot.yml` per `CLAUDE.md`:
  - `package-ecosystem: gradle`, `directory: /`, `schedule: weekly`, `groups: { dependencies: { patterns: ["*"] } }`
  - `package-ecosystem: github-actions`, `directory: /`, `schedule: weekly`
- **Verify:** `dependabot.yml` is valid YAML; Dependabot enabled in repo settings → PRs appear within a week

### Step 96 ⬜ — Add PR template + branch protection docs
- Create `.github/pull_request_template.md` with sections: Summary, Type of change, Checklist (tests, docs, Spotless)
- Create `docs/github-setup.md` documenting branch protection rules to apply manually:
  - Require PR reviews (1 approver)
  - Require status checks: CI workflow jobs
  - No direct pushes to `main`
- **Verify:** opening a PR pre-fills the template

