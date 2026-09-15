package com.apisentinel.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateApiRequest(
        @NotNull(message = "Application ID is required")
        UUID applicationId,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Provider is required")
        String provider, // e.g. "OPENAI", "GEMINI", "ANTHROPIC", "MOCK", "CUSTOM"

        @NotBlank(message = "Base URL is required")
        String baseUrl,

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
