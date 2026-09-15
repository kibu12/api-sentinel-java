package com.apisentinel.gateway;

import java.util.Map;

public record GatewayRequest(
        String method,
        String path,
        Map<String, String> headers,
        String body,
        Integer inputUnits,
        Integer outputUnits
) {}
