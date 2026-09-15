package com.apisentinel.budget;

import com.apisentinel.api.ApiConfiguration;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiConfiguration apiConfiguration;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType; // "DAILY", "MONTHLY"

    @Column(name = "limit_amount", nullable = false, precision = 12, scale = 4)
    private BigDecimal limitAmount;

    @Column(name = "warning_percent", nullable = false)
    private int warningPercent;

    @Column(name = "critical_percent", nullable = false)
    private int criticalPercent;

    @Column(name = "blocking_enabled", nullable = false)
    private boolean blockingEnabled;

    @Column(name = "current_period_start", nullable = false)
    private Instant currentPeriodStart;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Budget() {}

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (this.currentPeriodStart == null) {
            this.currentPeriodStart = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.warningPercent <= 0) {
            this.warningPercent = 80;
        }
        if (this.criticalPercent <= 0) {
            this.criticalPercent = 90;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ApiConfiguration getApiConfiguration() { return apiConfiguration; }
    public void setApiConfiguration(ApiConfiguration apiConfiguration) { this.apiConfiguration = apiConfiguration; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public int getWarningPercent() { return warningPercent; }
    public void setWarningPercent(int warningPercent) { this.warningPercent = warningPercent; }
    public int getCriticalPercent() { return criticalPercent; }
    public void setCriticalPercent(int criticalPercent) { this.criticalPercent = criticalPercent; }
    public boolean isBlockingEnabled() { return blockingEnabled; }
    public void setBlockingEnabled(boolean blockingEnabled) { this.blockingEnabled = blockingEnabled; }
    public Instant getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(Instant currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
