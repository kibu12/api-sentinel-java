package com.apisentinel.quota;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.exception.QuotaExceededException;
import com.apisentinel.usage.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class QuotaServiceTest {

    private UsageRecordRepository usageRecordRepository;
    private QuotaService quotaService;

    @BeforeEach
    void setUp() {
        usageRecordRepository = Mockito.mock(UsageRecordRepository.class);
        quotaService = new QuotaService(usageRecordRepository);
    }

    @Test
    void testDailyQuotaNotExceeded() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");
        api.setDailyQuota(100);
        api.setMonthlyQuota(1000);

        when(usageRecordRepository.countByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(50L);

        assertDoesNotThrow(() -> quotaService.checkQuota(api));
    }

    @Test
    void testDailyQuotaExceededThrows() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");
        api.setDailyQuota(100);
        api.setMonthlyQuota(1000);

        when(usageRecordRepository.countByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(105L);

        QuotaExceededException ex = assertThrows(
                QuotaExceededException.class,
                () -> quotaService.checkQuota(api)
        );

        assertEquals("QUOTA_EXCEEDED", ex.getCode());
    }
}
