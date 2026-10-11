# 🚖 CabFlow — Production-Grade Cab Booking Platform

A complete, enterprise-grade, full-stack cab-booking application inspired by Uber, Ola, and Lyft. Built with a modern **Java 21 LTS + Spring Boot 3** backend and a reactive **React 18 + TypeScript + Vite + Tailwind CSS** frontend.

---

## 🌟 Key Features

### Rider Experience
- **Interactive Map Booking**: OpenStreetMap + Leaflet integration with interactive click-to-pin, address reverse geocoding, and polyline route preview.
- **Dynamic Fare Engine**: Haversine distance calculation with road curvature compensation (1.25x), vehicle multiplier (Hatchback, Sedan, SUV, Premium), duration estimation, and real-time promo code deductions.
- **Live Ride Tracking**: Real-time STOMP WebSocket driver location streaming (`/topic/rides/{id}/location`) and status lifecycle updates (`/topic/rides/{id}`).
- **Receipts & History**: Complete ride archive with paginated history and downloadable PDF receipts generated via OpenPDF (`%PDF-` compliant).
- **Ratings & Reviews**: 1–5 star driver rating and feedback system.
- **Saved Places**: Rapid one-click booking to Home, Work, and favorite spots.

### Driver Experience
- **Registration & Vehicle Onboarding**: Multi-stage registration capturing license, vehicle make/model/year, seating capacity, and registration number.
- **Live Dispatch & Countdown**: Incoming ride requests broadcast via private WebSocket channels with a 30-second acceptance countdown timer.
- **Concurrency-Safe Ride Assignment**: Atomic SQL conditional assignment (`UPDATE ... WHERE status='REQUESTED' AND driver IS NULL`) preventing race conditions between competing drivers.
- **Trip Lifecycle Controls**: Step-by-step progress updating (Accepted → Driver Arriving → In Progress → Completed).
- **Earnings & Analytics**: Daily, weekly, monthly, and lifetime earnings calculation with commission deductions and trip statistics.

### Admin Experience
- **Operational Metrics**: Real-time KPI dashboard tracking total users, active drivers, current active trips, gross revenue, and pending verifications.
- **Driver Verification Pipeline**: Document and vehicle verification workflow to approve or reject applicant drivers.
- **User Management**: Searchable user table with one-click suspension and reactivation.
- **Ride Monitoring**: Live monitoring table of all rides across all statuses with filtering.

---

## 🏗️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21 LTS, Spring Boot 3.4.3, Spring Data JPA / Hibernate, Spring Security 6, JJWT 0.12.6 |
| **Realtime** | Spring WebSocket (STOMP over SockJS) |
| **Database** | PostgreSQL 16 (production), Flyway Migrations, H2 in-memory (testing/dev) |
| **Payments** | Razorpay Java SDK 1.4.8 (Order creation, HMAC-SHA256 signature verification, webhooks, refunds) |
| **PDF Generation** | OpenPDF 2.0.3 |
| **API Docs** | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| **Frontend** | React 18, TypeScript (strict), Vite 5, Tailwind CSS, TanStack Query v5 |
| **Forms & Validation** | React Hook Form, Zod |
| **Maps** | Leaflet, React-Leaflet, OpenStreetMap tiles |
| **HTTP & WS Clients** | Axios (with token rotation interceptor), @stomp/stompjs, SockJS-client |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Vitest |
| **Containers & CI** | Docker, Multi-stage Dockerfiles, Docker Compose, Nginx, GitHub Actions |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Maven 3.9+** (`mvn -v`)
- **Node.js 20+** and **npm** (`node -v`, `npm -v`)
- *(Optional)* Docker & Docker Compose for containerized setup

---

### Option A: Run Locally (Development Mode)

#### 1. Start Backend (Uses H2 in-memory DB by default, no PostgreSQL password needed)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- API Base URL: `http://localhost:8080/api/v1`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- Spring Actuator Health: `http://localhost:8080/actuator/health`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cabflow_dev`, user: `sa`, no password)

#### 2. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
- Access the web application at: `http://localhost:5173`

---

### Option B: Run with Docker Compose (Production Environment)

From the project root:
```bash
cd infrastructure
docker compose up --build
```
This spins up:
- **PostgreSQL 16**: Port 5432
- **Redis 7**: Port 6379
- **Spring Boot Backend**: Port 8080
- **Nginx + React Frontend**: Port 80 and 3000

---

## 🔑 Seeded Demo Credentials

The backend automatically seeds ready-to-test accounts upon startup (non-test profile):

| Role | Email | Password | Details |
|---|---|---|---|
| **Administrator** | `admin@cabflow.app` | `Admin@123` | Full access to stats, user control, driver verification |
| **Rider** | `rider@cabflow.app` | `Rider@123` | Active rider account with booking privileges |
| **Driver 1** | `driver@cabflow.app` | `Driver@123` | Verified driver (Toyota Camry Sedan, Times Square area) |
| **Driver 2** | `driver2@cabflow.app` | `Driver@123` | Verified driver (Honda CR-V SUV, Midtown area) |

### Active Promo Codes
- `WELCOME20` — 20% discount on first ride
- `FLAT5OFF` — \$5 flat discount on any ride over \$10
- `VIPRIDE` — \$10 discount for premium bookings

---

## 🧪 Running Automated Tests

### Backend Unit & Integration Tests (13/13 passing)
```bash
cd backend
mvn test
```
Tests cover:
- Haversine distance calculation and pricing rules (`PricingServiceTest`)
- Concurrency-safe driver dispatch (`DispatchConcurrencyTest`)
- State machine transition restrictions (`RideStateTransitionTest`)
- OpenPDF receipt byte generation (`PdfReceiptGeneratorTest`)
- JWT registration and authentication flow (`AuthenticationIntegrationTest`)

### Frontend Tests (Vitest)
```bash
cd frontend
npm test
```

### Frontend Production Build Verification
```bash
cd frontend
npm run build
```

---

## 📐 Architecture Overview

```
React (TypeScript + Leaflet)
       │
       ▼ (REST / STOMP over SockJS)
Spring Security 6 (JWT + Refresh Tokens)
       │
       ▼
Controllers (Auth, Rider, Driver, Ride, Pricing, Payment, Admin)
       │
       ▼
Service Layer (Atomic Dispatch, Fare Calculator, Razorpay, Notifications)
       │
       ▼
Spring Data JPA & Hibernate
       │
       ▼
PostgreSQL 16 / Flyway Migrations
```

### Concurrency Safety
Driver assignment employs an atomic conditional database query:
```sql
UPDATE rides
SET driver_id = :driverId, status = 'ACCEPTED'
WHERE id = :rideId AND status = 'REQUESTED' AND driver_id IS NULL;
```
If two drivers accept simultaneously, exactly one row is updated and the second driver receives a friendly conflict notice (`Ride already claimed`).

---

## 📚 Official Learning Resources & Documentation Links

- **Spring Boot**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **Spring Security**: [https://spring.io/projects/spring-security](https://spring.io/projects/spring-security)
- **Hibernate ORM**: [https://hibernate.org/orm/](https://hibernate.org/orm/)
- **Razorpay Documentation**: [https://razorpay.com/docs/payments/payment-gateway/server-integration/java/](https://razorpay.com/docs/payments/payment-gateway/server-integration/java/)
- **Spring WebSocket**: [https://docs.spring.io/spring-framework/reference/web/websocket.html](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- **PostgreSQL**: [https://www.postgresql.org/docs/](https://www.postgresql.org/docs/)
- **Docker**: [https://docs.docker.com/](https://docs.docker.com/)

---

## 📄 License & Attribution
Designed and built for production standard cab hailing architecture. All rights reserved.
