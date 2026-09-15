package com.apisentinel.anomaly;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.quota.QuotaService;
import com.apisentinel.usage.UsageRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class AnomalyDetector {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetector.class);

    private final AnomalyRepository anomalyRepository;
    private final UsageRecordRepository usageRecordRepository;

    public AnomalyDetector(AnomalyRepository anomalyRepository, UsageRecordRepository usageRecordRepository) {
        this.anomalyRepository = anomalyRepository;
        this.usageRecordRepository = usageRecordRepository;
    }

    @Transactional
    public void evaluate(ApiConfiguration api, int currentStatusCode, BigDecimal currentCost) {
        try {
            evaluateErrorRate(api);
            evaluateTrafficSpike(api);
            evaluateCostSurge(api);
        } catch (Exception e) {
            log.error("Error evaluating anomalies for API [{}]: {}", api.getId(), e.getMessage());
        }
    }

    private void evaluateErrorRate(ApiConfiguration api) {
        Instant twoMinutesAgo = Instant.now().minus(2, ChronoUnit.MINUTES);
        long totalRecent = usageRecordRepository.countByApiIdSince(api.getId(), twoMinutesAgo);
        if (totalRecent < 3) return;

        long errorsRecent = usageRecordRepository.count5xxByApiIdSince(api.getId(), twoMinutesAgo);
        double errorRate = (double) errorsRecent / totalRecent;

        if (errorRate >= 0.10) { // 10% threshold
            triggerAnomaly(
                    api,
                    "HIGH_5XX_RATE",
                    "HIGH",
                    BigDecimal.valueOf(Math.round(errorRate * 100.0)),
                    BigDecimal.valueOf(10.0),
                    BigDecimal.valueOf(10.0),
                    String.format("5xx error rate reached %.1f%% in the last 2 minutes (%d errors out of %d requests)",
                            errorRate * 100.0, errorsRecent, totalRecent)
            );
        }
    }

    private void evaluateTrafficSpike(ApiConfiguration api) {
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        long count1m = usageRecordRepository.countByApiIdSince(api.getId(), oneMinuteAgo);

        // Baseline: expected requests per minute is rateLimit / 4 or at least 5
        long baseline = Math.max(5, api.getRateLimitPerMinute() / 4);

        if (count1m >= 10 && count1m > (baseline * 3)) {
            triggerAnomaly(
                    api,
                    "TRAFFIC_SPIKE",
                    "MEDIUM",
                    BigDecimal.valueOf(count1m),
                    BigDecimal.valueOf(baseline),
                    BigDecimal.valueOf(baseline * 3),
                    String.format("Traffic surged to %d requests in the last minute (baseline: %d/min)", count1m, baseline)
            );
        }
    }

    private void evaluateCostSurge(ApiConfiguration api) {
        if (api.getDailyBudget() == null || api.getDailyBudget().compareTo(BigDecimal.ZERO) <= 0) return;

        Instant startOfDay = QuotaService.getStartOfUtcDay();
        BigDecimal dailySpend = usageRecordRepository.sumCostByApiIdSince(api.getId(), startOfDay);

        // If daily spend exceeds 80% of daily budget
        BigDecimal warningThreshold = api.getDailyBudget().multiply(new BigDecimal("0.80"));
        if (dailySpend.compareTo(warningThreshold) >= 0) {
            triggerAnomaly(
                    api,
                    "COST_SPIKE",
                    "CRITICAL",
                    dailySpend.setScale(2, RoundingMode.HALF_UP),
                    warningThreshold.setScale(2, RoundingMode.HALF_UP),
                    api.getDailyBudget().setScale(2, RoundingMode.HALF_UP),
                    String.format("Daily spend reached $%s, exceeding 80%% of the $%s daily budget cap",
                            dailySpend.toPlainString(), api.getDailyBudget().toPlainString())
            );
        }
    }

    private void triggerAnomaly(
            ApiConfiguration api,
            String type,
            String severity,
            BigDecimal observed,
            BigDecimal expected,
            BigDecimal threshold,
            String description) {

        // Deduplication rule: do not recreate if an OPEN anomaly of this type already exists for this API
        Optional<Anomaly> existing = anomalyRepository.findFirstByApiConfigurationAndTypeAndStatus(api, type, "OPEN");
        if (existing.isPresent()) {
            log.debug("Deduplicated anomaly [{}] for API [{}]; active alert exists", type, api.getName());
            return;
        }

        Anomaly anomaly = new Anomaly();
        anomaly.setApiConfiguration(api);
        anomaly.setType(type);
        anomaly.setSeverity(severity);
        anomaly.setObservedValue(observed);
        anomaly.setExpectedValue(expected);
        anomaly.setThreshold(threshold);
        anomaly.setStatus("OPEN");
        anomaly.setDescription(description);

        anomalyRepository.save(anomaly);
        log.warn("ALERT: Anomaly [{}] triggered for API [{}]: {}", type, api.getName(), description);
    }
}
