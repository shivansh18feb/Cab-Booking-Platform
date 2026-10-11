# Security Hardening Checklist

- [x] **Authentication & Password Storage**: BCrypt with strength factor 12. No plain-text passwords stored or logged.
- [x] **JWT Security**: Signed with HMAC-SHA256 (minimum 256-bit secret). Short-lived access token (15 mins) and rotating refresh token.
- [x] **Role-Based Access Control**: PreAuthorize annotations on sensitive controller endpoints.
- [x] **Data Isolation (IDOR Prevention)**: Riders can only view/cancel their own rides; drivers can only update assigned rides.
- [x] **Injection Prevention**: Hibernate parameterized queries via Spring Data JPA. No string concatenation in queries.
- [x] **XSS Mitigation**: React automatic JSX string escaping; strict Content Security Policy headers.
- [x] **CORS Configuration**: Whitelisted origins only (`http://localhost:5173`, etc.), no wildcard `*` with credentials.
- [x] **Stripe Security**: Webhook secret signature verified against raw payload bytes before processing events.
- [x] **Safe Logging**: Zero logging of credit cards, passwords, or full JWT tokens in application logs.
- [x] **Actuator Security**: Non-sensitive health endpoint public; administrative metrics restricted.
