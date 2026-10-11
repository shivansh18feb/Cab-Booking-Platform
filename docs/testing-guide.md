# Testing Guide & Quality Assurance Strategy

## 1. Testing Pyramid Overview

- **Unit Tests**: Domain logic, fare estimation, state transitions, authentication token generation, password hashing.
- **Integration Tests**: Spring Boot `@SpringBootTest` with MockMvc covering REST endpoints, security filters, transactional rollbacks, and concurrent requests.
- **Concurrency & Race Condition Tests**: Multi-threaded execution testing simultaneous driver acceptance of a single ride.
- **Frontend Tests**: Component validation, authentication routing, state handling with Vitest & React Testing Library.
- **End-to-End Simulation**: Full booking lifecycle test from rider request through driver acceptance, trip progress, completion, and payment.

## 2. Running Test Suites

### Backend Tests
```bash
cd backend
mvn clean test
```

### Frontend Tests
```bash
cd frontend
npm run test
```

## 3. Concurrency Safety Test Strategy
To test that two drivers cannot claim the same ride concurrently:
1. Seed a ride with `REQUESTED` status.
2. Spin up two executor threads simulating Driver A and Driver B accepting the ride at the exact same millisecond.
3. Assert that exactly one thread succeeds with `200 OK` and the other returns `409 Conflict` (Ride already claimed or invalid state).
4. Assert the database records exactly one assigned driver.
