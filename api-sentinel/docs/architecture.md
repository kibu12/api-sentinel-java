# API Sentinel — System Architecture & Design Document

## 1. Overview
API Sentinel is an enterprise-grade API governance and protection gateway operating between consuming applications and upstream external/internal APIs. It enforces request velocity rate limits (Token Bucket), period quotas (Daily/Monthly), monetary spending caps (with 80%/90% warnings and 100% blocking), Redis caching, resilience patterns (timeouts, bounded exponential retries, Resilience4j circuit breakers), and anomaly detection.

## 2. Request Lifecycle (The Engineering North Star)
```
Request 
  ──> Correlation / Request ID Generation (req-UUID)
  ──> API Key Authentication & Tenant Verification (SHA-256 Key Hash)
  ──> API Configuration Status Check (Active vs. Disabled)
  ──> Rate Limit Evaluation (Bucket4j Token Bucket: Capacity & Refill)
  ──> Quota Decision (Daily / Monthly Request Counts)
  ──> Budget Decision (Daily / Monthly Spend Caps with Blocking)
  ──> Redis Cache Lookup (Cache Hit returns early without Upstream call)
  ──> Upstream Forwarding (SSRF-protected registered endpoints)
  ──> Resilience Handling (Timeouts, Bounded Retries, Circuit Breaker)
  ──> Usage Record Persistence (Status, Latency, Units, Cost)
  ──> Anomaly Evaluation (Traffic Spike, Error Rate Spike, Cost Surge)
  ──> Structured Observability & Client Response
```

## 3. Data Model Architecture
- **Users**: Multi-tenant isolation with user role (`USER`, `ADMIN`).
- **Applications**: Logical consuming application container owned by a user.
- **API Configurations**: Target upstream service definitions with associated policy limits.
- **API Keys**: High-entropy keys (stored as SHA-256 hashes, displayed only via safe prefix `apis_...`).
- **Pricing Rules**: Versioned pricing models (per-request or unit/token based).
- **Usage Records**: Audit log of every request, outcome, status code, latency, and monetary cost.
- **Budgets**: Spending thresholds per period with warning and critical flags.
- **Anomalies**: Automated detection of abnormal behavior with lifecycle status (`OPEN`, `ACKNOWLEDGED`, `RESOLVED`).
- **Audit Logs**: Administrative and security event tracking.

## 4. Security & Isolation Controls
- Cross-tenant isolation: Users cannot access or query resources owned by other users.
- SSRF prevention: Forwarding occurs strictly to registered, validated upstream base URLs.
- Sensitive data filtering: Secrets, passwords, raw keys, and authorization headers are never written to disk or logs.
