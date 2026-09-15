package com.apisentinel.api;

import com.apisentinel.application.Application;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_configurations")
public class ApiConfiguration {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String provider; // e.g. "OPENAI", "GEMINI", "ANTHROPIC", "MOCK", "CUSTOM"

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(nullable = false)
    private String status; // "ACTIVE", "DISABLED"

    @Column(name = "rate_limit_per_minute", nullable = false)
    private int rateLimitPerMinute;

    @Column(name = "daily_quota", nullable = false)
    private int dailyQuota;

    @Column(name = "monthly_quota", nullable = false)
    private int monthlyQuota;

    @Column(name = "daily_budget", nullable = false, precision = 12, scale = 4)
    private BigDecimal dailyBudget;

    @Column(name = "monthly_budget", nullable = false, precision = 12, scale = 4)
    private BigDecimal monthlyBudget;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs;

    @Column(name = "cache_enabled", nullable = false)
    private boolean cacheEnabled;

    @Column(name = "cache_ttl_seconds", nullable = false)
    private int cacheTtlSeconds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ApiConfiguration() {}

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.rateLimitPerMinute <= 0) {
            this.rateLimitPerMinute = 60;
        }
        if (this.dailyQuota <= 0) {
            this.dailyQuota = 1000;
        }
        if (this.monthlyQuota <= 0) {
            this.monthlyQuota = 20000;
        }
        if (this.dailyBudget == null) {
            this.dailyBudget = new BigDecimal("50.0000");
        }
        if (this.monthlyBudget == null) {
            this.monthlyBudget = new BigDecimal("1000.0000");
        }
        if (this.timeoutMs <= 0) {
            this.timeoutMs = 5000;
        }
        if (this.cacheTtlSeconds <= 0) {
            this.cacheTtlSeconds = 60;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public int getDailyQuota() { return dailyQuota; }
    public void setDailyQuota(int dailyQuota) { this.dailyQuota = dailyQuota; }
    public int getMonthlyQuota() { return monthlyQuota; }
    public void setMonthlyQuota(int monthlyQuota) { this.monthlyQuota = monthlyQuota; }
    public BigDecimal getDailyBudget() { return dailyBudget; }
    public void setDailyBudget(BigDecimal dailyBudget) { this.dailyBudget = dailyBudget; }
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }
    public int getCacheTtlSeconds() { return cacheTtlSeconds; }
    public void setCacheTtlSeconds(int cacheTtlSeconds) { this.cacheTtlSeconds = cacheTtlSeconds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
