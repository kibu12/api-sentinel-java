package com.apisentinel.ratelimit;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void testRateLimitWithinCapacity() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");
        api.setRateLimitPerMinute(5);

        UUID keyId = UUID.randomUUID();

        // 5 requests should pass
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimitService.checkRateLimit(api, keyId));
        }

        // 6th request must throw RateLimitExceededException (429)
        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> rateLimitService.checkRateLimit(api, keyId)
        );

        assertTrue(ex.getRetryAfterSeconds() >= 1);
        assertEquals("RATE_LIMIT_EXCEEDED", ex.getCode());
    }
}
