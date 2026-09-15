package com.apisentinel.anomaly;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.usage.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AnomalyDetectorTest {

    private AnomalyRepository anomalyRepository;
    private UsageRecordRepository usageRecordRepository;
    private AnomalyDetector anomalyDetector;

    @BeforeEach
    void setUp() {
        anomalyRepository = Mockito.mock(AnomalyRepository.class);
        usageRecordRepository = Mockito.mock(UsageRecordRepository.class);
        anomalyDetector = new AnomalyDetector(anomalyRepository, usageRecordRepository);
    }

    @Test
    void testTriggers5xxAnomalyWhenExceeding10Percent() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");
        api.setRateLimitPerMinute(100); // Prevents traffic spike triggering

        when(usageRecordRepository.countByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(20L); // 20 total requests
        when(usageRecordRepository.count5xxByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(5L);  // 5 errors = 25% > 10%
        when(anomalyRepository.findFirstByApiConfigurationAndTypeAndStatus(eq(api), eq("HIGH_5XX_RATE"), eq("OPEN")))
                .thenReturn(Optional.empty());
        when(anomalyRepository.findFirstByApiConfigurationAndTypeAndStatus(eq(api), eq("TRAFFIC_SPIKE"), eq("OPEN")))
                .thenReturn(Optional.of(new Anomaly())); // traffic spike suppressed

        anomalyDetector.evaluate(api, 500, BigDecimal.ZERO);

        verify(anomalyRepository, times(1)).save(any(Anomaly.class));
    }

    @Test
    void testDeduplicatesActiveAnomalies() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");
        api.setRateLimitPerMinute(100);

        when(usageRecordRepository.countByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(20L);
        when(usageRecordRepository.count5xxByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(5L);

        // An active OPEN anomaly already exists!
        Anomaly existing = new Anomaly();
        existing.setStatus("OPEN");
        when(anomalyRepository.findFirstByApiConfigurationAndTypeAndStatus(eq(api), eq("HIGH_5XX_RATE"), eq("OPEN")))
                .thenReturn(Optional.of(existing));
        when(anomalyRepository.findFirstByApiConfigurationAndTypeAndStatus(eq(api), eq("TRAFFIC_SPIKE"), eq("OPEN")))
                .thenReturn(Optional.of(existing));

        anomalyDetector.evaluate(api, 500, BigDecimal.ZERO);

        // Verify deduplication: save must NOT be called again
        verify(anomalyRepository, never()).save(any(Anomaly.class));
    }
}
