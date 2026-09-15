package com.apisentinel.mock;

import com.apisentinel.common.ApiResponse;
import com.apisentinel.gateway.GatewayRequest;
import com.apisentinel.gateway.GatewayResponse;
import com.apisentinel.gateway.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulator")
public class TrafficSimulatorController {

    private final GatewayService gatewayService;

    public TrafficSimulatorController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    public record BurstRequest(
            UUID apiId,
            String apiKey,
            Integer count,
            Boolean simulateErrors
    ) {}

    @PostMapping("/burst")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateBurst(@RequestBody BurstRequest request) {
        int total = (request.count() != null && request.count() > 0) ? Math.min(request.count(), 100) : 15;
        boolean errors = Boolean.TRUE.equals(request.simulateErrors());

        int successes = 0;
        int rateLimited = 0;
        int budgetExceeded = 0;
        int serverErrors = 0;

        for (int i = 0; i < total; i++) {
            try {
                String subPath = errors ? "/fail" : "/echo";
                GatewayRequest req = new GatewayRequest("GET", subPath, null, null, 10, 10);
                GatewayResponse resp = gatewayService.handleRequest(request.apiId(), request.apiKey(), req, null);
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    successes++;
                } else {
                    serverErrors++;
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("Rate limit")) {
                    rateLimited++;
                } else if (msg.contains("Budget exceeded") || msg.contains("BUDGET_EXCEEDED")) {
                    budgetExceeded++;
                } else {
                    serverErrors++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAttempted", total);
        result.put("successCount", successes);
        result.put("rateLimitedCount", rateLimited);
        result.put("budgetExceededCount", budgetExceeded);
        result.put("errorCount", serverErrors);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
