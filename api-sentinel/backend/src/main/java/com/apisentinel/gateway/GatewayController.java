package com.apisentinel.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/gateway/v1")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/{apiId}/request")
    public ResponseEntity<GatewayResponse> handleRequest(
            @PathVariable UUID apiId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestBody(required = false) GatewayRequest request,
            HttpServletRequest servletRequest) {

        if (request == null) {
            request = new GatewayRequest("GET", "", null, null, 0, 0);
        }

        GatewayResponse response = gatewayService.handleRequest(apiId, apiKey, request, requestId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", response.requestId());
        headers.set("X-Cache-Status", response.cacheHit() ? "HIT" : "MISS");

        return ResponseEntity
                .status(response.statusCode())
                .headers(headers)
                .body(response);
    }
}
