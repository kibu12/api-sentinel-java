package com.apisentinel.api;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record UpdateApiRequest(
        String name,
        String provider,
        String baseUrl,
        String status, // "ACTIVE", "DISABLED"

        @Min(value = 1, message = "Rate limit must be at least 1 per minute")
        Integer rateLimitPerMinute,

        @Min(value = 1, message = "Daily quota must be at least 1")
        Integer dailyQuota,

        @Min(value = 1, message = "Monthly quota must be at least 1")
        Integer monthlyQuota,

        BigDecimal dailyBudget,
        BigDecimal monthlyBudget,

        @Min(value = 100, message = "Timeout must be at least 100ms")
        Integer timeoutMs,

        Boolean cacheEnabled,

        @Min(value = 1, message = "Cache TTL must be at least 1 second")
        Integer cacheTtlSeconds
) {}
