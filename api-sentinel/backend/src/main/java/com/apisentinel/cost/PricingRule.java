package com.apisentinel.cost;

import com.apisentinel.api.ApiConfiguration;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiConfiguration apiConfiguration;

    @Column(name = "pricing_type", nullable = false)
    private String pricingType; // "PER_REQUEST" or "PER_UNIT"

    @Column(name = "request_price", precision = 12, scale = 6)
    private BigDecimal requestPrice;

    @Column(name = "input_unit_price", precision = 12, scale = 6)
    private BigDecimal inputUnitPrice;

    @Column(name = "output_unit_price", precision = 12, scale = 6)
    private BigDecimal outputUnitPrice;

    @Column(name = "unit_name", length = 50)
    private String unitName;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(nullable = false)
    private boolean active;

    public PricingRule() {}

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.effectiveFrom == null) {
            this.effectiveFrom = Instant.now();
        }
        if (this.requestPrice == null) {
            this.requestPrice = BigDecimal.ZERO;
        }
        if (this.inputUnitPrice == null) {
            this.inputUnitPrice = BigDecimal.ZERO;
        }
        if (this.outputUnitPrice == null) {
            this.outputUnitPrice = BigDecimal.ZERO;
        }
        if (this.unitName == null) {
            this.unitName = "token";
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ApiConfiguration getApiConfiguration() { return apiConfiguration; }
    public void setApiConfiguration(ApiConfiguration apiConfiguration) { this.apiConfiguration = apiConfiguration; }
    public String getPricingType() { return pricingType; }
    public void setPricingType(String pricingType) { this.pricingType = pricingType; }
    public BigDecimal getRequestPrice() { return requestPrice; }
    public void setRequestPrice(BigDecimal requestPrice) { this.requestPrice = requestPrice; }
    public BigDecimal getInputUnitPrice() { return inputUnitPrice; }
    public void setInputUnitPrice(BigDecimal inputUnitPrice) { this.inputUnitPrice = inputUnitPrice; }
    public BigDecimal getOutputUnitPrice() { return outputUnitPrice; }
    public void setOutputUnitPrice(BigDecimal outputUnitPrice) { this.outputUnitPrice = outputUnitPrice; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
