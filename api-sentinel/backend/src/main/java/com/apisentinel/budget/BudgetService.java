package com.apisentinel.budget;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.exception.BudgetExceededException;
import com.apisentinel.exception.ForbiddenException;
import com.apisentinel.exception.ResourceNotFoundException;
import com.apisentinel.quota.QuotaService;
import com.apisentinel.usage.UsageRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final ApiConfigurationRepository apiRepository;
    private final ApplicationRepository applicationRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public BudgetService(
            BudgetRepository budgetRepository,
            ApiConfigurationRepository apiRepository,
            ApplicationRepository applicationRepository,
            UsageRecordRepository usageRecordRepository,
            AuthService authService,
            AuditService auditService) {
        this.budgetRepository = budgetRepository;
        this.apiRepository = apiRepository;
        this.applicationRepository = applicationRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public void checkBudget(ApiConfiguration api) {
        List<Budget> budgets = budgetRepository.findByApiConfiguration(api);

        for (Budget budget : budgets) {
            Instant since = "DAILY".equalsIgnoreCase(budget.getPeriodType())
                    ? QuotaService.getStartOfUtcDay()
                    : QuotaService.getStartOfUtcMonth();

            BigDecimal currentSpend = usageRecordRepository.sumCostByApiIdSince(api.getId(), since);

            if (budget.getLimitAmount() != null && budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (currentSpend.compareTo(budget.getLimitAmount()) >= 0) {
                    log.warn("Budget limit exceeded for API [{}]: Spend ${} >= Limit ${} (Period: {})",
                            api.getName(), currentSpend, budget.getLimitAmount(), budget.getPeriodType());

                    if (budget.isBlockingEnabled()) {
                        throw new BudgetExceededException(
                                "Budget exceeded for API '" + api.getName() + "'. Current spend: $" +
                                        currentSpend.toPlainString() + " exceeds " + budget.getPeriodType().toLowerCase() +
                                        " limit of $" + budget.getLimitAmount().toPlainString()
                        );
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> getUserBudgets() {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) return Collections.emptyList();

        List<ApiConfiguration> apis = apiRepository.findByApplicationIn(apps);
        List<BudgetDto> result = new ArrayList<>();

        for (ApiConfiguration api : apis) {
            List<Budget> budgets = budgetRepository.findByApiConfiguration(api);
            for (Budget b : budgets) {
                Instant since = "DAILY".equalsIgnoreCase(b.getPeriodType())
                        ? QuotaService.getStartOfUtcDay()
                        : QuotaService.getStartOfUtcMonth();
                BigDecimal currentSpend = usageRecordRepository.sumCostByApiIdSince(api.getId(), since);
                result.add(BudgetDto.from(b, currentSpend));
            }
        }
        return result;
    }

    @Transactional
    public List<BudgetDto> updateApiBudgets(UUID apiId, UpdateBudgetRequest request) {
        User currentUser = authService.getCurrentUser();
        ApiConfiguration api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API Configuration not found: " + apiId));

        if (!api.getApplication().getOwner().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You are not authorized to configure budgets for this API");
        }

        String targetPeriod = request.periodType() != null ? request.periodType().toUpperCase() : "DAILY";

        Budget budget = budgetRepository.findByApiConfigurationAndPeriodType(api, targetPeriod)
                .orElseGet(() -> {
                    Budget nb = new Budget();
                    nb.setApiConfiguration(api);
                    nb.setPeriodType(targetPeriod);
                    return nb;
                });

        if (request.limitAmount() != null) {
            budget.setLimitAmount(request.limitAmount());
            if ("DAILY".equalsIgnoreCase(targetPeriod)) {
                api.setDailyBudget(request.limitAmount());
            } else {
                api.setMonthlyBudget(request.limitAmount());
            }
            apiRepository.save(api);
        }
        if (request.warningPercent() != null) budget.setWarningPercent(request.warningPercent());
        if (request.criticalPercent() != null) budget.setCriticalPercent(request.criticalPercent());
        if (request.blockingEnabled() != null) budget.setBlockingEnabled(request.blockingEnabled());

        budgetRepository.save(budget);

        auditService.record(currentUser, "BUDGET_UPDATED", "BUDGET", budget.getId().toString(),
                "Updated " + targetPeriod + " budget to $" + budget.getLimitAmount());

        return getUserBudgets();
    }
}
