package com.apisentinel.gateway;

import com.apisentinel.anomaly.AnomalyDetector;
import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.apikey.ApiKey;
import com.apisentinel.apikey.ApiKeyService;
import com.apisentinel.budget.BudgetService;
import com.apisentinel.cache.CacheService;
import com.apisentinel.cost.CostCalculator;
import com.apisentinel.cost.PricingRule;
import com.apisentinel.cost.PricingRuleRepository;
import com.apisentinel.exception.ApiDisabledException;
import com.apisentinel.exception.ResourceNotFoundException;
import com.apisentinel.exception.SentinelException;
import com.apisentinel.quota.QuotaService;
import com.apisentinel.ratelimit.RateLimitService;
import com.apisentinel.resilience.ResilienceService;
import com.apisentinel.usage.UsageRecord;
import com.apisentinel.usage.UsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final ApiKeyService apiKeyService;
    private final ApiConfigurationRepository apiRepository;
    private final RateLimitService rateLimitService;
    private final QuotaService quotaService;
    private final BudgetService budgetService;
    private final CacheService cacheService;
    private final ResilienceService resilienceService;
    private final UpstreamClient upstreamClient;
    private final CostCalculator costCalculator;
    private final PricingRuleRepository pricingRuleRepository;
    private final UsageService usageService;
    private final AnomalyDetector anomalyDetector;
    private final ObjectMapper objectMapper;

    public GatewayService(
            ApiKeyService apiKeyService,
            ApiConfigurationRepository apiRepository,
            RateLimitService rateLimitService,
            QuotaService quotaService,
            BudgetService budgetService,
            CacheService cacheService,
            ResilienceService resilienceService,
            UpstreamClient upstreamClient,
            CostCalculator costCalculator,
            PricingRuleRepository pricingRuleRepository,
            UsageService usageService,
            AnomalyDetector anomalyDetector,
            ObjectMapper objectMapper) {
        this.apiKeyService = apiKeyService;
        this.apiRepository = apiRepository;
        this.rateLimitService = rateLimitService;
        this.quotaService = quotaService;
        this.budgetService = budgetService;
        this.cacheService = cacheService;
        this.resilienceService = resilienceService;
        this.upstreamClient = upstreamClient;
        this.costCalculator = costCalculator;
        this.pricingRuleRepository = pricingRuleRepository;
        this.usageService = usageService;
        this.anomalyDetector = anomalyDetector;
        this.objectMapper = objectMapper;
    }

    public GatewayResponse handleRequest(
            UUID apiId,
            String rawApiKey,
            GatewayRequest request,
            String incomingRequestId) {

        long startTime = System.currentTimeMillis();
        String requestId = (incomingRequestId != null && !incomingRequestId.isBlank())
                ? incomingRequestId
                : "req-" + UUID.randomUUID().toString().substring(0, 8);

        // 1. Authenticate API Key
        ApiKey apiKey = apiKeyService.validateApiKey(rawApiKey);

        // 2. Load API Configuration
        ApiConfiguration api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API Configuration not found: " + apiId));

        // Validate API status
        if (!"ACTIVE".equalsIgnoreCase(api.getStatus())) {
            recordRejection(requestId, api, apiKey, 403, "API_DISABLED");
            throw new ApiDisabledException("API '" + api.getName() + "' is currently disabled");
        }

        // 3. Rate Limit Check
        try {
            rateLimitService.checkRateLimit(api, apiKey.getId());
        } catch (SentinelException e) {
            recordRejection(requestId, api, apiKey, e.getHttpStatus().value(), e.getCode());
            throw e;
        }

        // 4. Quota Check
        try {
            quotaService.checkQuota(api);
        } catch (SentinelException e) {
            recordRejection(requestId, api, apiKey, e.getHttpStatus().value(), e.getCode());
            throw e;
        }

        // 5. Budget Check
        try {
            budgetService.checkBudget(api);
        } catch (SentinelException e) {
            recordRejection(requestId, api, apiKey, e.getHttpStatus().value(), e.getCode());
            throw e;
        }

        String method = request.method() != null ? request.method().toUpperCase() : "GET";
        String path = request.path() != null ? request.path() : "";
        String body = request.body();
        int inUnits = request.inputUnits() != null ? request.inputUnits() : 0;
        int outUnits = request.outputUnits() != null ? request.outputUnits() : 0;

        // 6. Redis Cache Check
        if (api.isCacheEnabled()) {
            String cacheKey = cacheService.generateKey(api.getId(), method, path, body);
            Optional<String> cached = cacheService.get(cacheKey);
            if (cached.isPresent()) {
                long latency = System.currentTimeMillis() - startTime;
                Object parsedCached = parseJson(cached.get());

                // Record cache hit usage
                persistUsage(requestId, api, apiKey, 200, latency, inUnits, outUnits, BigDecimal.ZERO, true, false, null);

                return new GatewayResponse(requestId, 200, latency, true, BigDecimal.ZERO, parsedCached);
            }
        }

        // 7. Construct target upstream URL (SSRF prevention: strictly based on api.getBaseUrl())
        String baseUrl = api.getBaseUrl().replaceAll("/+$", "");
        String subPath = path.startsWith("/") ? path : "/" + path;
        String fullUrl = baseUrl + subPath;

        // 8. Forward Upstream with Resilience (Timeouts, Retries, Circuit Breaker)
        UpstreamClient.UpstreamResult result;
        try {
            result = resilienceService.executeWithResilience(api.getId(), () ->
                    upstreamClient.forward(fullUrl, method, request.headers(), body, api.getTimeoutMs())
            );
        } catch (SentinelException e) {
            recordRejection(requestId, api, apiKey, e.getHttpStatus().value(), e.getCode());
            throw e;
        } catch (Exception e) {
            recordRejection(requestId, api, apiKey, 500, "INTERNAL_ERROR");
            throw new RuntimeException("Gateway execution failure: " + e.getMessage(), e);
        }

        long latency = System.currentTimeMillis() - startTime;

        // 9. If Cacheable and Successful, store in Cache
        if (api.isCacheEnabled() && result.statusCode() >= 200 && result.statusCode() < 300) {
            String cacheKey = cacheService.generateKey(api.getId(), method, path, body);
            cacheService.put(cacheKey, result.body(), api.getCacheTtlSeconds());
        }

        // 10. Calculate Cost
        PricingRule pricingRule = pricingRuleRepository
                .findFirstByApiConfigurationAndActiveTrueOrderByEffectiveFromDesc(api)
                .orElse(null);
        BigDecimal cost = costCalculator.calculateCost(pricingRule, inUnits, outUnits);

        // 11. Persist Usage Record
        persistUsage(requestId, api, apiKey, result.statusCode(), latency, inUnits, outUnits, cost, false, false, null);

        // 12. Evaluate Anomaly Rules
        anomalyDetector.evaluate(api, result.statusCode(), cost);

        Object parsedPayload = parseJson(result.body());
        return new GatewayResponse(requestId, result.statusCode(), latency, false, cost, parsedPayload);
    }

    private void recordRejection(
            String requestId,
            ApiConfiguration api,
            ApiKey apiKey,
            int statusCode,
            String rejectionReason) {

        persistUsage(requestId, api, apiKey, statusCode, 1, 0, 0, BigDecimal.ZERO, false, true, rejectionReason);
    }

    private void persistUsage(
            String requestId,
            ApiConfiguration api,
            ApiKey apiKey,
            int statusCode,
            long latencyMs,
            int inputUnits,
            int outputUnits,
            BigDecimal estimatedCost,
            boolean cacheHit,
            boolean rejected,
            String rejectionReason) {

        UsageRecord record = new UsageRecord();
        record.setRequestId(requestId);
        record.setApiConfiguration(api);
        record.setApplication(api.getApplication());
        record.setApiKey(apiKey);
        record.setStatusCode(statusCode);
        record.setLatencyMs(latencyMs);
        record.setInputUnits(inputUnits);
        record.setOutputUnits(outputUnits);
        record.setEstimatedCost(estimatedCost);
        record.setCacheHit(cacheHit);
        record.setRejected(rejected);
        record.setRejectionReason(rejectionReason);

        usageService.recordUsage(record);
    }

    private Object parseJson(String text) {
        if (text == null || text.isBlank()) return "";
        try {
            return objectMapper.readValue(text, Object.class);
        } catch (Exception e) {
            return text;
        }
    }
}
