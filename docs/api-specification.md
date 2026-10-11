# REST API & WebSocket Specification

## 1. Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new rider | No |
| `POST` | `/api/v1/auth/driver/register` | Register a new driver with vehicle details | No |
| `POST` | `/api/v1/auth/login` | Authenticate with email/password; returns JWT + Refresh Token | No |
| `POST` | `/api/v1/auth/refresh` | Rotate access token using valid refresh token | No |
| `POST` | `/api/v1/auth/logout` | Revoke active refresh token | Yes |
| `GET`  | `/api/v1/auth/me` | Fetch authenticated user profile and roles | Yes |

## 2. Ride Management (`/api/v1/rides`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/rides/estimate` | Calculate fare breakdown & ETA for pickup/dropoff | Yes (Rider) |
| `POST` | `/api/v1/rides` | Create a new ride booking (REQUESTED) | Yes (Rider) |
| `GET`  | `/api/v1/rides/{id}` | Get ride details and tracking data | Yes (Rider/Driver/Admin) |
| `POST` | `/api/v1/rides/{id}/cancel` | Cancel ride (with cancellation rules) | Yes (Rider/Driver) |
| `GET`  | `/api/v1/rides/{id}/events` | Fetch audit timeline for the ride | Yes (Rider/Driver/Admin) |
| `GET`  | `/api/v1/rides/{id}/receipt` | Download PDF receipt / invoice | Yes (Rider) |

## 3. Driver Operations (`/api/v1/drivers`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET`  | `/api/v1/drivers/me` | Current driver profile, vehicle & stats | Yes (Driver) |
| `PATCH`| `/api/v1/drivers/me/availability` | Toggle status (`ONLINE`, `OFFLINE`) | Yes (Driver) |
| `POST` | `/api/v1/drivers/me/location` | Update GPS coordinates | Yes (Driver) |
| `GET`  | `/api/v1/drivers/me/requests` | Check pending dispatched ride requests | Yes (Driver) |
| `POST` | `/api/v1/drivers/rides/{id}/accept` | Accept dispatched ride (concurrency safe) | Yes (Driver) |
| `POST` | `/api/v1/drivers/rides/{id}/reject` | Reject dispatched ride | Yes (Driver) |
| `POST` | `/api/v1/drivers/rides/{id}/arrived` | Mark arrived at pickup location | Yes (Driver) |
| `POST` | `/api/v1/drivers/rides/{id}/start` | Start trip with rider | Yes (Driver) |
| `POST` | `/api/v1/drivers/rides/{id}/complete`| Complete ride and finalize fare | Yes (Driver) |
| `GET`  | `/api/v1/drivers/me/earnings` | Earnings summary & breakdown | Yes (Driver) |

## 4. Payment & Billing (`/api/v1/payments`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/payments/intent` | Create Stripe PaymentIntent for completed ride | Yes (Rider) |
| `POST` | `/api/v1/payments/confirm` | Confirm payment with transaction reference | Yes (Rider) |
| `POST` | `/api/v1/webhooks/stripe` | Stripe signed webhook receiver | No (Signature Verified) |

## 5. WebSocket STOMP Topics & Events (`/ws`)

| Destination | Direction | Description |
|---|---|---|
| `/topic/rides/{rideId}` | Server -> Subscribed Clients | Emits ride state transitions & driver GPS coords |
| `/topic/drivers/{driverId}/requests` | Server -> Driver Client | Dispatches incoming booking requests |
| `/topic/riders/{riderId}` | Server -> Rider Client | Direct alerts (e.g. driver assigned, arrival) |
| `/app/driver/location` | Driver Client -> Server | Ingests real-time latitude & longitude updates |
