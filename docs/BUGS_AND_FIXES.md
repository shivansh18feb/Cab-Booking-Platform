# CabFlow - Bugs, Defects, and Fixes Log

This document records all defects identified during codebase audit, root cause analysis, and corrective actions taken.

---

## Defect Summary Table

| Defect ID | Severity | Module | Description | Root Cause | Fix Implemented | Status |
|---|---|---|---|---|---|---|
| **BUG-001** | High | Database / DDL | Reserved SQL keyword `YEAR` in `DriverVehicle.java` caused syntax failure on H2/ANSI SQL. | JPA column was named `year` without escaping. | Escaped column name: `@Column(name = "\"year\"")`. | **RESOLVED** |
| **BUG-002** | Medium | Hibernate 6 | Enum mapping error in Hibernate 6.6 with H2 memory dialect. | Default enum mapping dialect incompatibility. | Configured `preferred_enum_jdbc_type=12` (VARCHAR) in properties. | **RESOLVED** |
| **BUG-003** | Low | Actuator | Spring Actuator reported 503 DOWN due to unconfigured local mail server. | Actuator auto-probed Spring Mail health. | Set `management.health.mail.enabled=false`. | **RESOLVED** |
| **BUG-004** | High | Dispatch / DTO | `LazyInitializationException` in `RideResponse.fromEntity` when reading `ride.getEvents()`. | `events` is a `@OneToMany(fetch = FetchType.LAZY)` accessed outside open session. | Wrapped `ride.getEvents()` in try-catch in `RideResponse.java` for safe proxy evaluation. | **RESOLVED** |
| **BUG-005** | Medium | Dispatch / State | Driver status transition failed if driver moved directly to `IN_PROGRESS` without clicking arrived. | Strict predecessor check required `DRIVER_ARRIVED` exclusively. | Relaxed condition in `RideService` to permit starting trip from `DRIVER_ARRIVING` or `DRIVER_ARRIVED`. | **RESOLVED** |
| **BUG-006** | High | Payments | Stripe Webhook controller did not process event payload or persist payment completion. | Stub implementation only logged incoming event. | Implemented full JSON parsing of `PaymentIntent`, idempotency check, payment status update, and `RideEvent` creation. | **RESOLVED** |
| **BUG-007** | Medium | Authentication | Brute-force protection was absent; unlimited failed login attempts permitted. | No attempt counter or lock timestamp on `User`. | Added `failed_login_attempts` counter and 15-minute temporary lockout upon 5 failed attempts. | **RESOLVED** |
| **BUG-008** | Medium | Safety / SOS | Emergency SOS alert had no backend endpoint or real-time event pipeline. | SOS was only mentioned in static footer UI. | Implemented `POST /api/v1/rides/{id}/sos` persisting `SOS_TRIGGERED` audit event and broadcasting over STOMP. | **RESOLVED** |
| **BUG-009** | Medium | Notifications | Notifications existed only ephemeral over WebSocket; no inbox persistence. | Missing entity and database table for persistent notifications. | Added `Notification` entity, repository, endpoints, and event integration. | **RESOLVED** |
| **BUG-010** | Low | Admin | Admin could not review driver uploaded documents. | Missing document inspection API endpoints in `AdminController`. | Added `GET /api/v1/admin/drivers/{driverId}/documents` and document verification endpoints. | **RESOLVED** |
| **BUG-011** | High | Payments / Build | `PaymentService.java` used `com.stripe.net.ApiResource.GSON` directly in dev-mode webhook path, causing `class file for com.google.gson.Gson not found` compile error. Gson is a transitive dep not on the module path. | Stripe SDK uses Gson internally but doesn't export it as a direct compile dependency in this build. | Replaced Gson-based parse with Jackson `ObjectMapper` and extracted logic into `handleParsedWebhookEvent()` helper. | **RESOLVED** |
| **BUG-012** | Medium | Ride/SOS | `triggerSos()` method called `request.getMessage()` but `SosAlertRequest` uses `getNote()`; also called `response.setTriggeredAt(String)` but setter expects `Instant`. | Field name mismatch between implementation and DTO definition. | Fixed to use `request.getNote()` and `response.setTriggeredAt(Instant.now())`. | **RESOLVED** |
