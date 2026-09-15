package com.apisentinel.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ApiConfigurationDto(
        UUID id,
        UUID applicationId,
        String applicationName,
        String name,
        String provider,
        String baseUrl,
        String status,
        int rateLimitPerMinute,
        int dailyQuota,
        int monthlyQuota,
        BigDecimal dailyBudget,
        BigDecimal monthlyBudget,
        int timeoutMs,
        boolean cacheEnabled,
        int cacheTtlSeconds,
        Instant createdAt,
        Instant updatedAt
) {
    public static ApiConfigurationDto from(ApiConfiguration entity) {
        return new ApiConfigurationDto(
                entity.getId(),
                entity.getApplication().getId(),
                entity.getApplication().getName(),
                entity.getName(),
                entity.getProvider(),
                entity.getBaseUrl(),
                entity.getStatus(),
                entity.getRateLimitPerMinute(),
                entity.getDailyQuota(),
                entity.getMonthlyQuota(),
                entity.getDailyBudget(),
                entity.getMonthlyBudget(),
                entity.getTimeoutMs(),
                entity.isCacheEnabled(),
                entity.getCacheTtlSeconds(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
