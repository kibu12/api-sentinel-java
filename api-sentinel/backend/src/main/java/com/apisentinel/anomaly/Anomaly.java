package com.apisentinel.anomaly;

import com.apisentinel.api.ApiConfiguration;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomalies")
public class Anomaly {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiConfiguration apiConfiguration;

    @Column(nullable = false, length = 50)
    private String type; // "TRAFFIC_SPIKE", "HIGH_5XX_RATE", "COST_SPIKE"

    @Column(nullable = false, length = 20)
    private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"

    @Column(name = "observed_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal observedValue;

    @Column(name = "expected_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal expectedValue;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal threshold;

    @Column(nullable = false, length = 30)
    private String status; // "OPEN", "ACKNOWLEDGED", "RESOLVED"

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Anomaly() {}

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.detectedAt == null) {
            this.detectedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = "OPEN";
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ApiConfiguration getApiConfiguration() { return apiConfiguration; }
    public void setApiConfiguration(ApiConfiguration apiConfiguration) { this.apiConfiguration = apiConfiguration; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public BigDecimal getObservedValue() { return observedValue; }
    public void setObservedValue(BigDecimal observedValue) { this.observedValue = observedValue; }
    public BigDecimal getExpectedValue() { return expectedValue; }
    public void setExpectedValue(BigDecimal expectedValue) { this.expectedValue = expectedValue; }
    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
