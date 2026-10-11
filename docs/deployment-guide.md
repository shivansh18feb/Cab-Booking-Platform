# Deployment Guide & Production Operations

## 1. Local Development Setup

### Option A: Maven & Vite Locally
1. Start PostgreSQL (e.g. on port 5432, database `cabbooking`).
2. Set environment variables or rely on dev profile with automatic fallback.
3. Backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### Option B: Docker Compose
```bash
docker compose -f infrastructure/docker-compose.yml up --build -d
```

## 2. Cloud Production Deployment Architecture

- **Frontend**: Hosted on Vercel / Cloudflare Pages / AWS S3 + CloudFront.
- **Backend**: Containerized Spring Boot service deployed on Render / Railway / AWS ECS Fargate.
- **Database**: Managed PostgreSQL instance (AWS RDS, Supabase, Neon, or Railway PostgreSQL).
- **SSL / TLS**: Automated via Cloudflare or AWS ACM certificates.
- **CORS & Domain Mapping**: Strict frontend origin whitelisting in production profile.
