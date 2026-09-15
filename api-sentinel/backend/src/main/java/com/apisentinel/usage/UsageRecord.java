package com.apisentinel.usage;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.apikey.ApiKey;
import com.apisentinel.application.Application;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_records")
public class UsageRecord {

    @Id
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true, length = 100)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiConfiguration apiConfiguration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id")
    private ApiKey apiKey;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "input_units")
    private int inputUnits;

    @Column(name = "output_units")
    private int outputUnits;

    @Column(name = "estimated_cost", nullable = false, precision = 12, scale = 6)
    private BigDecimal estimatedCost;

    @Column(name = "cache_hit", nullable = false)
    private boolean cacheHit;

    @Column(nullable = false)
    private boolean rejected;

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UsageRecord() {}

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.estimatedCost == null) {
            this.estimatedCost = BigDecimal.ZERO;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public ApiConfiguration getApiConfiguration() { return apiConfiguration; }
    public void setApiConfiguration(ApiConfiguration apiConfiguration) { this.apiConfiguration = apiConfiguration; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    public ApiKey getApiKey() { return apiKey; }
    public void setApiKey(ApiKey apiKey) { this.apiKey = apiKey; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public int getInputUnits() { return inputUnits; }
    public void setInputUnits(int inputUnits) { this.inputUnits = inputUnits; }
    public int getOutputUnits() { return outputUnits; }
    public void setOutputUnits(int outputUnits) { this.outputUnits = outputUnits; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public boolean isCacheHit() { return cacheHit; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
    public boolean isRejected() { return rejected; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
