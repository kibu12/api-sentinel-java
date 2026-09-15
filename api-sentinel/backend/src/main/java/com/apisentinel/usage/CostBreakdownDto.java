package com.apisentinel.usage;

import java.math.BigDecimal;
import java.util.UUID;

public record CostBreakdownDto(
        UUID apiId,
        String apiName,
        long requestCount,
        BigDecimal totalCost
) {}
