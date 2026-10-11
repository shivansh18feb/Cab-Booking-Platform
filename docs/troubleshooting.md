# Operational Troubleshooting Guide

## 1. Common Issues & Resolutions

### Database Connection Failure
- **Symptom**: `Connection to localhost:5432 refused` or `FATAL: password authentication failed`.
- **Cause**: PostgreSQL service is down or database credentials mismatch.
- **Fix**: Verify PostgreSQL service status via `Get-Service *postgres*` or start Docker PostgreSQL. Ensure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` match your environment. In development mode, the application falls back gracefully to in-memory H2 if no local PostgreSQL instance is reachable.

### WebSocket Connection Drop / Stale State
- **Symptom**: Real-time GPS movement stops updating or connection states flip to disconnected.
- **Cause**: Network blip or proxy disconnect.
- **Fix**: The React frontend includes automated exponential backoff reconnection. Upon reconnecting, the frontend initiates a REST state synchronization call (`GET /api/v1/rides/{id}`) to re-sync any missed transitions.

### Stripe Webhook Signature Mismatch
- **Symptom**: `400 Bad Request` or `SignatureVerificationException` in Stripe webhook handler.
- **Cause**: Incorrect `STRIPE_WEBHOOK_SECRET` or payload tampering by a middleware.
- **Fix**: Verify that the webhook request body is received raw and unmodified, and ensure `STRIPE_WEBHOOK_SECRET` matches your Stripe CLI or dashboard webhook secret.
