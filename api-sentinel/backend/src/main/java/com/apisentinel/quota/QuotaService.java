package com.apisentinel.quota;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.exception.QuotaExceededException;
import com.apisentinel.usage.UsageRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class QuotaService {

    private static final Logger log = LoggerFactory.getLogger(QuotaService.class);
    private final UsageRecordRepository usageRecordRepository;

    public QuotaService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    public void checkQuota(ApiConfiguration api) {
        Instant startOfDay = getStartOfUtcDay();
        long dailyCount = usageRecordRepository.countByApiIdSince(api.getId(), startOfDay);

        if (dailyCount >= api.getDailyQuota()) {
            log.warn("Daily quota exceeded for API [{}]: {} / {}", api.getName(), dailyCount, api.getDailyQuota());
            throw new QuotaExceededException("Daily quota of " + api.getDailyQuota() + " requests exceeded for API '" + api.getName() + "'");
        }

        Instant startOfMonth = getStartOfUtcMonth();
        long monthlyCount = usageRecordRepository.countByApiIdSince(api.getId(), startOfMonth);

        if (monthlyCount >= api.getMonthlyQuota()) {
            log.warn("Monthly quota exceeded for API [{}]: {} / {}", api.getName(), monthlyCount, api.getMonthlyQuota());
            throw new QuotaExceededException("Monthly quota of " + api.getMonthlyQuota() + " requests exceeded for API '" + api.getName() + "'");
        }
    }

    public static Instant getStartOfUtcDay() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    public static Instant getStartOfUtcMonth() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }
}
