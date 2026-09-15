package com.apisentinel.usage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UsageRecordDto(
        UUID id,
        String requestId,
        UUID apiId,
        String apiName,
        UUID applicationId,
        String applicationName,
        UUID apiKeyId,
        int statusCode,
        long latencyMs,
        int inputUnits,
        int outputUnits,
        BigDecimal estimatedCost,
        boolean cacheHit,
        boolean rejected,
        String rejectionReason,
        Instant createdAt
) {
    public static UsageRecordDto from(UsageRecord record) {
        return new UsageRecordDto(
                record.getId(),
                record.getRequestId(),
                record.getApiConfiguration().getId(),
                record.getApiConfiguration().getName(),
                record.getApplication().getId(),
                record.getApplication().getName(),
                record.getApiKey() != null ? record.getApiKey().getId() : null,
                record.getStatusCode(),
                record.getLatencyMs(),
                record.getInputUnits(),
                record.getOutputUnits(),
                record.getEstimatedCost(),
                record.isCacheHit(),
                record.isRejected(),
                record.getRejectionReason(),
                record.getCreatedAt()
        );
    }
}
