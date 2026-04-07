# Phase 11 — Security

> **Steps 87–88** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 87 ✅ — Add Spring Security (stateless JWT)
- Add `spring-boot-starter-security` and `spring-security-oauth2-resource-server` to `libs.versions.toml`
- Create `shared/infrastructure/config/SecurityConfig.java`
  - **Public endpoints** (no auth required):
    - `GET /api/v1/products/**`
    - `GET /api/v1/categories/**`
    - `GET /api/v1/supermarkets/**`
    - `GET /api/v1/compare`
    - `/actuator/health`
    - `/swagger-ui/**`, `/v3/api-docs/**`
  - **Authenticated** (JWT required): everything else (baskets, sync trigger, SyncRun query)
- **Verify:** `@WebMvcTest` on `BasketController` — 401 without token, 200 with valid JWT; public endpoints return 200 without token

### Step 88 ✅ — Add OWASP Dependency Check to build
- Add `owasp-dependency-check` plugin to `libs.versions.toml`
- Configure in `build.gradle.kts` with `failBuildOnCVSS = 7.0f`
- Create `config/owasp-suppressions.xml` for known false positives
- **Verify:** `./gradlew dependencyCheckAnalyze` completes; no unaddressed CVEs >= 7.0

