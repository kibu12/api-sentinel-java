# API Sentinel — Enterprise API Governance Gateway

API Sentinel is an API cost, abuse, and reliability control platform that sits between client applications and external/internal upstream APIs. It authenticates clients, enforces velocity limits and spending caps, caches safe responses in Redis, isolates unhealthy providers with circuit breakers, logs structured usage, and provides operational visibility through a modern dashboard.

---

## 1. System Architecture

```
Client Request
      │
      ▼
Spring Boot Gateway (/gateway/v1/{apiId}/request)
      ├── 1. Generate Request ID (req-UUID)
      ├── 2. Authenticate API Key (SHA-256 Lookup)
      ├── 3. Validate Target API Status (Active/Disabled)
      ├── 4. Token-Bucket Rate Limiter (Bucket4j: 429 Retry-After)
      ├── 5. Period Quota Engine (Daily / Monthly Request Counts)
      ├── 6. Spending Cap Engine (80%/90% Alert, 100% Block)
      ├── 7. Redis Cache Lookup (Hit -> Early Return)
      ├── 8. Upstream HTTP Forwarder (SSRF-Protected)
      ├── 9. Resilience4j Layer (Timeout, Exponential Retry, Circuit Breaker)
      ├── 10. Cost Calculator (BigDecimal: Per-Request / Per-Unit Pricing)
      ├── 11. Anomaly Detector (Traffic Spikes, 5xx Spikes, Spend Surges)
      └── 12. Structured Response + Usage Audit Record
```

---

## 2. Tech Stack

- **Backend**: Java 21, Spring Boot 3.3.x, Spring Web, Spring Security, Spring Data JPA, Hibernate, Bean Validation, Actuator
- **Database & Migration**: PostgreSQL 16 (H2 test profile), Flyway
- **Cache & Rate Limiting**: Redis 7, Bucket4j Token Bucket
- **Resilience**: Resilience4j CircuitBreaker, Retry, TimeLimiter
- **Security**: JWT HMAC-SHA256, BCrypt Password Hashing, SHA-256 Key Hashes
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, Recharts, Lucide Icons
- **Infrastructure**: Docker & Docker Compose

---

## 3. Prerequisites

- **Java 21 JDK** installed
- **Node.js 18+** & **npm** installed
- **Docker** & **Docker Compose** installed (optional for development, required for multi-container PostgreSQL & Redis)

---

## 4. Quickstart Guide

### Step 1: Start Infrastructure (PostgreSQL & Redis)
From the repository root (`api-sentinel/`):
```bash
docker compose up -d
```

### Step 2: Start Backend (Spring Boot)
From `api-sentinel/backend/`:
```bash
# Using installed Maven
mvn clean spring-boot:run

# Or with active dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
The backend starts on `http://localhost:8080`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`

### Step 3: Start Frontend (React + Vite)
From `api-sentinel/frontend/`:
```bash
npm install
npm run dev
```
The frontend UI will be available at `http://localhost:5173`.

---

## 5. Seed Demo Accounts & Keys

When starting on a fresh database, the system automatically initializes:
- **Developer User**: `demo@apisentinel.dev` / `password123`
- **Administrator**: `admin@apisentinel.dev` / `admin123`
- **Pre-Configured Demo API Key**: `sen_live_demo_key_1234567890abcdef`
- **Seed API 1 (Echo Mock)**: Rate limit: 10/min, Daily Budget: $10.00, Caching: Enabled
- **Seed API 2 (Flaky Upstream)**: For resilience & circuit breaker demonstration

---

## 6. Testing & Reproducing the Runaway-Traffic Demo

You can run the full demo locally without external API credits:

1. **Log in** at `http://localhost:5173` using `demo@apisentinel.dev` / `password123`.
2. Go to the **Dashboard**.
3. Use the **One-Click Traffic & Protection Simulator**:
   - Select **15 Requests (Trigger Rate-Limit)** and click **Fire Burst**.
   - Watch the first 10 requests succeed (`200 OK`) while requests 11–15 are rejected with `429 Too Many Requests`.
   - Check the **Active Budget Caps** to see calculated spend progress.
   - Inspect **Recent Security & Traffic Anomalies** as the surge triggers alert records.
4. Open **Usage Logs** to inspect per-request latency, costs, and cache hit statuses.

---

## 7. Running Automated Tests

Run the complete test suite (Unit, Integration, Security, and Concurrency tests):
```bash
cd backend
mvn test
```
