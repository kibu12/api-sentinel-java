package com.apisentinel.api;

import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.application.ApplicationService;
import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.budget.Budget;
import com.apisentinel.budget.BudgetRepository;
import com.apisentinel.common.PagedResponse;
import com.apisentinel.cost.PricingRule;
import com.apisentinel.cost.PricingRuleRepository;
import com.apisentinel.exception.ForbiddenException;
import com.apisentinel.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ApiService {

    private final ApiConfigurationRepository apiRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final PricingRuleRepository pricingRuleRepository;
    private final BudgetRepository budgetRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public ApiService(
            ApiConfigurationRepository apiRepository,
            ApplicationRepository applicationRepository,
            ApplicationService applicationService,
            PricingRuleRepository pricingRuleRepository,
            BudgetRepository budgetRepository,
            AuthService authService,
            AuditService auditService) {
        this.apiRepository = apiRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.pricingRuleRepository = pricingRuleRepository;
        this.budgetRepository = budgetRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional
    public ApiConfigurationDto createApi(CreateApiRequest request) {
        Application application = applicationService.getApplicationEntity(request.applicationId());
        User currentUser = authService.getCurrentUser();

        ApiConfiguration api = new ApiConfiguration();
        api.setApplication(application);
        api.setName(request.name().trim());
        api.setProvider(request.provider().trim().toUpperCase());
        api.setBaseUrl(request.baseUrl().trim());
        api.setStatus("ACTIVE");

        if (request.rateLimitPerMinute() != null) api.setRateLimitPerMinute(request.rateLimitPerMinute());
        if (request.dailyQuota() != null) api.setDailyQuota(request.dailyQuota());
        if (request.monthlyQuota() != null) api.setMonthlyQuota(request.monthlyQuota());
        if (request.dailyBudget() != null) api.setDailyBudget(request.dailyBudget());
        if (request.monthlyBudget() != null) api.setMonthlyBudget(request.monthlyBudget());
        if (request.timeoutMs() != null) api.setTimeoutMs(request.timeoutMs());
        if (request.cacheEnabled() != null) api.setCacheEnabled(request.cacheEnabled());
        if (request.cacheTtlSeconds() != null) api.setCacheTtlSeconds(request.cacheTtlSeconds());

        api = apiRepository.save(api);

        // Seed default pricing rule ($0.002 per request)
        PricingRule pricingRule = new PricingRule();
        pricingRule.setApiConfiguration(api);
        pricingRule.setPricingType("PER_REQUEST");
        pricingRule.setRequestPrice(new BigDecimal("0.002000"));
        pricingRule.setActive(true);
        pricingRule.setEffectiveFrom(Instant.now());
        pricingRuleRepository.save(pricingRule);

        // Seed default daily & monthly budgets
        Budget dailyBudget = new Budget();
        dailyBudget.setApiConfiguration(api);
        dailyBudget.setPeriodType("DAILY");
        dailyBudget.setLimitAmount(api.getDailyBudget());
        dailyBudget.setWarningPercent(80);
        dailyBudget.setCriticalPercent(90);
        dailyBudget.setBlockingEnabled(true);
        budgetRepository.save(dailyBudget);

        Budget monthlyBudget = new Budget();
        monthlyBudget.setApiConfiguration(api);
        monthlyBudget.setPeriodType("MONTHLY");
        monthlyBudget.setLimitAmount(api.getMonthlyBudget());
        monthlyBudget.setWarningPercent(80);
        monthlyBudget.setCriticalPercent(90);
        monthlyBudget.setBlockingEnabled(true);
        budgetRepository.save(monthlyBudget);

        auditService.record(currentUser, "API_REGISTERED", "API_CONFIGURATION", api.getId().toString(), "Registered API: " + api.getName());

        return ApiConfigurationDto.from(api);
    }

    @Transactional(readOnly = true)
    public List<ApiConfigurationDto> getUserApis() {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) return Collections.emptyList();

        return apiRepository.findByApplicationIn(apps).stream()
                .map(ApiConfigurationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiConfigurationDto> getUserApisPaged(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), 0, pageable.getPageSize(), 0, 0, true);
        }

        Page<ApiConfigurationDto> page = apiRepository.findByApplicationIn(apps, pageable)
                .map(ApiConfigurationDto::from);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ApiConfigurationDto getApiById(UUID id) {
        ApiConfiguration api = getApiEntity(id);
        return ApiConfigurationDto.from(api);
    }

    @Transactional
    public ApiConfigurationDto updateApi(UUID id, UpdateApiRequest request) {
        ApiConfiguration api = getApiEntity(id);
        User currentUser = authService.getCurrentUser();

        if (request.name() != null && !request.name().isBlank()) api.setName(request.name().trim());
        if (request.provider() != null && !request.provider().isBlank()) api.setProvider(request.provider().trim().toUpperCase());
        if (request.baseUrl() != null && !request.baseUrl().isBlank()) api.setBaseUrl(request.baseUrl().trim());
        if (request.status() != null && !request.status().isBlank()) api.setStatus(request.status().trim().toUpperCase());
        if (request.rateLimitPerMinute() != null) api.setRateLimitPerMinute(request.rateLimitPerMinute());
        if (request.dailyQuota() != null) api.setDailyQuota(request.dailyQuota());
        if (request.monthlyQuota() != null) api.setMonthlyQuota(request.monthlyQuota());
        if (request.dailyBudget() != null) api.setDailyBudget(request.dailyBudget());
        if (request.monthlyBudget() != null) api.setMonthlyBudget(request.monthlyBudget());
        if (request.timeoutMs() != null) api.setTimeoutMs(request.timeoutMs());
        if (request.cacheEnabled() != null) api.setCacheEnabled(request.cacheEnabled());
        if (request.cacheTtlSeconds() != null) api.setCacheTtlSeconds(request.cacheTtlSeconds());

        api = apiRepository.save(api);
        auditService.record(currentUser, "API_UPDATED", "API_CONFIGURATION", api.getId().toString(), "Updated config/status: " + api.getStatus());

        return ApiConfigurationDto.from(api);
    }

    @Transactional
    public void deleteApi(UUID id) {
        ApiConfiguration api = getApiEntity(id);
        User currentUser = authService.getCurrentUser();

        apiRepository.delete(api);
        auditService.record(currentUser, "API_DELETED", "API_CONFIGURATION", id.toString(), "Deleted API: " + api.getName());
    }

    public ApiConfiguration getApiEntity(UUID id) {
        User currentUser = authService.getCurrentUser();
        ApiConfiguration api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Configuration not found: " + id));

        // Enforce strict multi-tenant authorization rule (AC-02)
        if (!api.getApplication().getOwner().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You are not authorized to access this API Configuration");
        }

        return api;
    }
}
