# Phase 6 — Scheduler (Daily Sync)

> **Steps 60–61** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 60 ⬜ — Add Spring Scheduler configuration
- Enable `@EnableScheduling` in a `SchedulerConfig.java` within `sync/infrastructure/config/`
- Create `sync/infrastructure/adapter/input/scheduler/DailySyncScheduler.java`
  - Cron: every day at 03:00 Europe/Madrid (`0 0 3 * * *` with `zone = "Europe/Madrid"`)
  - Iterates over all active supermarkets and triggers `SyncSupermarketCatalogUseCase`
- **Verify:** unit test asserting scheduler calls use case for each supermarket; cron expression validated

### Step 61 ⬜ — Make scheduler configurable + add ShedLock
- Add `shedlock-spring` and `shedlock-provider-jdbc-template` to `libs.versions.toml`
- Create `V8__create_shedlock_table.sql` (standard ShedLock schema)
- Annotate scheduler method with `@SchedulerLock(name = "dailySync", lockAtMostFor = "2h")` to prevent concurrent execution on multi-instance deployments
- Add `app.scheduler.sync.enabled=true` property; use `@ConditionalOnProperty` to disable in tests
- **Verify:** `@DataJpaTest` confirming ShedLock table created; scheduler disabled in test profile via `app.scheduler.sync.enabled=false`

