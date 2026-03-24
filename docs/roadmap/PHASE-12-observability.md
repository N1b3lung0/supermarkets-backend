# Phase 12 — Observability Complete

> **Steps 89–91** | [← Index](../../ROADMAP.md)

## Progress Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔄 | In progress |
| ✅ | Done |

---

### Step 89 ⬜ — Add business metrics to key handlers
- `SyncSupermarketCatalogHandler`:
  - `sync.products.synced.total{supermarket}` (counter)
  - `sync.categories.synced.total{supermarket}` (counter)
  - `sync.duration.seconds{supermarket}` (timer)
- `CompareProductsByNameHandler`:
  - `comparisons.executed.total` (counter)
  - `comparisons.results.count` (histogram — number of matches per search)
- `RecordProductPriceHandler`:
  - `prices.recorded.total{supermarket}` (counter)
- **Verify:** `GET /actuator/metrics` returns the new metric names after triggering operations

### Step 90 ⬜ — Add Prometheus + Grafana to compose.yaml
- Add `prom/prometheus:latest` service to `compose.yaml`
- Add `config/prometheus.yml` with scrape config pointing to `app:8080/actuator/prometheus`
- Add `grafana/grafana:latest` service to `compose.yaml` with provisioned datasource + dashboard
- Import a basic Grafana dashboard JSON (`config/grafana/dashboards/supermarkets.json`) for JVM + business metrics
- **Verify:** `docker compose up -d` → Prometheus at `localhost:9090` scrapes metrics; Grafana dashboard at `localhost:3000` shows data

### Step 91 ⬜ — Add distributed tracing (OpenTelemetry)
- Add `otel-collector` (or Jaeger all-in-one) to `compose.yaml`
- Configure `micrometer-tracing` OTLP exporter in `application.yaml`:
  ```yaml
  management:
    tracing:
      sampling:
        probability: 1.0
  otel:
    exporter:
      otlp:
        endpoint: http://otel-collector:4318
  ```
- Verify `traceId` appears in JSON logs for each HTTP request
- **Verify:** a sync request generates a trace visible in Jaeger UI; `traceId` and `spanId` present in log output

