package com.apisentinel.usage;

import java.math.BigDecimal;

public record UsageSummaryDto(
        long totalRequests,
        BigDecimal totalCost,
        long errorCount,
        double errorRatePercentage,
        long cacheHitCount,
        double cacheHitPercentage,
        long activeApisCount,
        long openAnomaliesCount
) {}
