# ecom-microservices

## Local Seed Data Notes

- Default seed user IDs are deterministic:
  - `demo-admin-001`
  - `demo-seller-001`
  - `demo-customer-001`
- Product seeds use `demo-seller-001` as seller.
- Order seeds use `demo-customer-001` as customer.
- If your local MongoDB `users` collection already has older random IDs, clear it once and restart `user-service` so deterministic IDs are recreated.

## Correlation Service (alerts + metrics + logs + traces)

- New service module: `correlation` (port `8091`)
- Endpoints:
  - `POST /api/alert` (Alertmanager webhook)
  - `GET /api/correlation/{alert_fingerprint}`
  - `GET /api/health`
- Local observability stack wiring is included in `additional/evaluate-prometheus`:
  - `prometheus/prometheus.yml` now routes alerts to Alertmanager
  - `alertmanager/alertmanager.yml` forwards alerts to `http://correlation:8091/api/alert`
  - `prometheus/alert-rules.yml` contains starter alerts for service down and p95 latency
