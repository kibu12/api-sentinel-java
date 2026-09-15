package com.apisentinel.anomaly;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyDto(
        UUID id,
        UUID apiId,
        String apiName,
        String type,
        String severity,
        BigDecimal observedValue,
        BigDecimal expectedValue,
        BigDecimal threshold,
        String status,
        Instant detectedAt,
        Instant resolvedAt,
        String description
) {
    public static AnomalyDto from(Anomaly a) {
        return new AnomalyDto(
                a.getId(),
                a.getApiConfiguration().getId(),
                a.getApiConfiguration().getName(),
                a.getType(),
                a.getSeverity(),
                a.getObservedValue(),
                a.getExpectedValue(),
                a.getThreshold(),
                a.getStatus(),
                a.getDetectedAt(),
                a.getResolvedAt(),
                a.getDescription()
        );
    }
}
