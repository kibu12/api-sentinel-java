-- Flyway V1: Initial Schema for API Sentinel

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE api_configurations (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    rate_limit_per_minute INT NOT NULL DEFAULT 60,
    daily_quota INT NOT NULL DEFAULT 1000,
    monthly_quota INT NOT NULL DEFAULT 20000,
    daily_budget NUMERIC(12, 4) NOT NULL DEFAULT 50.0000,
    monthly_budget NUMERIC(12, 4) NOT NULL DEFAULT 1000.0000,
    timeout_ms INT NOT NULL DEFAULT 5000,
    cache_enabled BOOLEAN NOT NULL DEFAULT false,
    cache_ttl_seconds INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE api_keys (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    key_prefix VARCHAR(32) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP
);

CREATE TABLE pricing_rules (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_configurations(id) ON DELETE CASCADE,
    pricing_type VARCHAR(50) NOT NULL,
    request_price NUMERIC(12, 6) DEFAULT 0.000000,
    input_unit_price NUMERIC(12, 6) DEFAULT 0.000000,
    output_unit_price NUMERIC(12, 6) DEFAULT 0.000000,
    unit_name VARCHAR(50) DEFAULT 'token',
    effective_from TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE usage_records (
    id UUID PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    api_id UUID NOT NULL REFERENCES api_configurations(id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    api_key_id UUID REFERENCES api_keys(id) ON DELETE SET NULL,
    status_code INT NOT NULL,
    latency_ms BIGINT NOT NULL,
    input_units INT DEFAULT 0,
    output_units INT DEFAULT 0,
    estimated_cost NUMERIC(12, 6) NOT NULL DEFAULT 0.000000,
    cache_hit BOOLEAN NOT NULL DEFAULT false,
    rejected BOOLEAN NOT NULL DEFAULT false,
    rejection_reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_configurations(id) ON DELETE CASCADE,
    period_type VARCHAR(20) NOT NULL,
    limit_amount NUMERIC(12, 4) NOT NULL,
    warning_percent INT NOT NULL DEFAULT 80,
    critical_percent INT NOT NULL DEFAULT 90,
    blocking_enabled BOOLEAN NOT NULL DEFAULT true,
    current_period_start TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE anomalies (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_configurations(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    observed_value NUMERIC(12, 4) NOT NULL,
    expected_value NUMERIC(12, 4) NOT NULL,
    threshold NUMERIC(12, 4) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    description TEXT
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Indexes for performance and query patterns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_applications_owner_id ON applications(owner_id);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_usage_records_request_id ON usage_records(request_id);
CREATE INDEX idx_usage_records_app_created ON usage_records(application_id, created_at);
CREATE INDEX idx_usage_records_api_created ON usage_records(api_id, created_at);
CREATE INDEX idx_anomalies_api_detected ON anomalies(api_id, detected_at);
CREATE INDEX idx_audit_logs_actor_created ON audit_logs(actor_user_id, created_at);
