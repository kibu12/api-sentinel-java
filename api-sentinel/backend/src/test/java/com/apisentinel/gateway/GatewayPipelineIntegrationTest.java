package com.apisentinel.gateway;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.apikey.ApiKey;
import com.apisentinel.apikey.ApiKeyRepository;
import com.apisentinel.apikey.ApiKeyService;
import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.auth.User;
import com.apisentinel.auth.UserRepository;
import com.apisentinel.usage.UsageRecord;
import com.apisentinel.usage.UsageRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GatewayPipelineIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApiConfigurationRepository apiRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    private User testUser;
    private Application testApp;
    private ApiConfiguration testApi;
    private String rawApiKey;
    private ApiKey apiKeyEntity;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("gw_user_" + System.currentTimeMillis() + "@test.com");
        testUser.setPasswordHash("dummy");
        testUser.setRole("ROLE_USER");
        testUser.setStatus("ACTIVE");
        testUser = userRepository.save(testUser);

        testApp = new Application();
        testApp.setOwner(testUser);
        testApp.setName("Gateway Test Application");
        testApp.setEnvironment("TEST");
        testApp.setStatus("ACTIVE");
        testApp = applicationRepository.save(testApp);

        testApi = new ApiConfiguration();
        testApi.setApplication(testApp);
        testApi.setName("Local Mock Echo API");
        testApi.setProvider("MOCK");
        testApi.setBaseUrl("http://localhost:" + port + "/mock-upstream");
        testApi.setStatus("ACTIVE");
        testApi.setRateLimitPerMinute(3); // Small limit for testing AC-07
        testApi.setDailyQuota(50);
        testApi.setMonthlyQuota(1000);
        testApi.setDailyBudget(new BigDecimal("50.0000"));
        testApi.setMonthlyBudget(new BigDecimal("1000.0000"));
        testApi.setTimeoutMs(5000);
        testApi.setCacheEnabled(true);
        testApi.setCacheTtlSeconds(60);
        testApi = apiRepository.save(testApi);

        rawApiKey = "sen_live_testkey_" + UUID.randomUUID().toString().replace("-", "");
        apiKeyEntity = new ApiKey();
        apiKeyEntity.setApplication(testApp);
        apiKeyEntity.setKeyPrefix("sen_live_testkey...");
        apiKeyEntity.setKeyHash(ApiKeyService.hashKey(rawApiKey));
        apiKeyEntity.setStatus("ACTIVE");
        apiKeyEntity = apiKeyRepository.save(apiKeyEntity);
    }

    @Test
    void testAC05_ValidGatewayRequestProxiesToMockUpstream() throws Exception {
        GatewayRequest request = new GatewayRequest("GET", "/echo", null, null, 10, 10);

        mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(header().string("X-Cache-Status", "MISS"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.cacheHit").value(false));

        // AC-10: Verify Usage Record is created in DB
        List<UsageRecord> records = usageRecordRepository.findAll();
        assertFalse(records.isEmpty());
    }

    @Test
    void testAC06_InvalidKeyRejectedBeforeUpstreamAccess() throws Exception {
        GatewayRequest request = new GatewayRequest("GET", "/echo", null, null, 0, 0);

        mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                        .header("X-API-Key", "invalid_bad_key_value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void testAC07_RateLimitRejectionReturns429() throws Exception {
        GatewayRequest request = new GatewayRequest("GET", "/echo", null, null, 0, 0);

        // Capacity is 3; send 3 requests
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                            .header("X-API-Key", rawApiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // 4th request must be rejected with 429 Too Many Requests
        mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void testAC11_CacheHitReturnsCachedResponse() throws Exception {
        GatewayRequest request = new GatewayRequest("POST", "/echo", null, "{\"message\":\"cacheable\"}", 0, 0);

        // First request: Cache MISS
        mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache-Status", "MISS"));

        // Second request with exact same payload: Cache HIT!
        mockMvc.perform(post("/gateway/v1/" + testApi.getId() + "/request")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache-Status", "HIT"))
                .andExpect(jsonPath("$.cacheHit").value(true));
    }
}
