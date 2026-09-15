package com.apisentinel.usage;

import com.apisentinel.anomaly.AnomalyRepository;
import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.common.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final ApplicationRepository applicationRepository;
    private final ApiConfigurationRepository apiRepository;
    private final AnomalyRepository anomalyRepository;
    private final AuthService authService;

    public UsageService(
            UsageRecordRepository usageRecordRepository,
            ApplicationRepository applicationRepository,
            ApiConfigurationRepository apiRepository,
            AnomalyRepository anomalyRepository,
            AuthService authService) {
        this.usageRecordRepository = usageRecordRepository;
        this.applicationRepository = applicationRepository;
        this.apiRepository = apiRepository;
        this.anomalyRepository = anomalyRepository;
        this.authService = authService;
    }

    @Transactional
    public UsageRecord recordUsage(UsageRecord record) {
        return usageRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UsageRecordDto> getUserUsageRecords(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), 0, pageable.getPageSize(), 0, 0, true);
        }

        Page<UsageRecordDto> page = usageRecordRepository.findByApplicationInOrderByCreatedAtDesc(apps, pageable)
                .map(UsageRecordDto::from);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UsageRecordDto> getApiUsageRecords(UUID apiId, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        ApiConfiguration api = apiRepository.findById(apiId)
                .orElseThrow(() -> new NoSuchElementException("API not found: " + apiId));

        if (!api.getApplication().getOwner().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new SecurityException("Access denied to API usage records");
        }

        Page<UsageRecordDto> page = usageRecordRepository.findByApiConfigurationOrderByCreatedAtDesc(api, pageable)
                .map(UsageRecordDto::from);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UsageSummaryDto getSummary() {
        User currentUser = authService.getCurrentUser();
        UUID ownerId = currentUser.getId();

        long totalRequests = usageRecordRepository.countByOwnerId(ownerId);
        BigDecimal totalCost = usageRecordRepository.sumCostByOwnerId(ownerId).setScale(4, RoundingMode.HALF_UP);
        long errorCount = usageRecordRepository.countErrorsByOwnerId(ownerId);
        long cacheHitCount = usageRecordRepository.countCacheHitsByOwnerId(ownerId);

        double errorRate = totalRequests > 0 ? ((double) errorCount / totalRequests) * 100.0 : 0.0;
        double cacheHitRate = totalRequests > 0 ? ((double) cacheHitCount / totalRequests) * 100.0 : 0.0;

        List<Application> apps = applicationRepository.findByOwner(currentUser);
        long activeApisCount = apps.isEmpty() ? 0 : apiRepository.findByApplicationIn(apps).stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .count();

        long openAnomaliesCount = anomalyRepository.countOpenByOwnerId(ownerId);

        return new UsageSummaryDto(
                totalRequests,
                totalCost,
                errorCount,
                Math.round(errorRate * 10.0) / 10.0,
                cacheHitCount,
                Math.round(cacheHitRate * 10.0) / 10.0,
                activeApisCount,
                openAnomaliesCount
        );
    }

    @Transactional(readOnly = true)
    public List<CostBreakdownDto> getCostBreakdown() {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) return Collections.emptyList();

        List<ApiConfiguration> apis = apiRepository.findByApplicationIn(apps);
        List<CostBreakdownDto> result = new ArrayList<>();

        for (ApiConfiguration api : apis) {
            long count = usageRecordRepository.countByApiIdSince(api.getId(), java.time.Instant.EPOCH);
            BigDecimal cost = usageRecordRepository.sumCostByApiIdSince(api.getId(), java.time.Instant.EPOCH);
            result.add(new CostBreakdownDto(api.getId(), api.getName(), count, cost.setScale(4, RoundingMode.HALF_UP)));
        }

        return result;
    }
}
