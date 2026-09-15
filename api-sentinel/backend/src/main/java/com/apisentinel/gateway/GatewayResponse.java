package com.apisentinel.gateway;

import java.math.BigDecimal;

public record GatewayResponse(
        String requestId,
        int statusCode,
        long latencyMs,
        boolean cacheHit,
        BigDecimal estimatedCost,
        Object payload
) {}
