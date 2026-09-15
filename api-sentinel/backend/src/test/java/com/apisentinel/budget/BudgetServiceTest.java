package com.apisentinel.budget;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.exception.BudgetExceededException;
import com.apisentinel.usage.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BudgetServiceTest {

    private BudgetRepository budgetRepository;
    private ApiConfigurationRepository apiRepository;
    private ApplicationRepository applicationRepository;
    private UsageRecordRepository usageRecordRepository;
    private AuthService authService;
    private AuditService auditService;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetRepository = Mockito.mock(BudgetRepository.class);
        apiRepository = Mockito.mock(ApiConfigurationRepository.class);
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        usageRecordRepository = Mockito.mock(UsageRecordRepository.class);
        authService = Mockito.mock(AuthService.class);
        auditService = Mockito.mock(AuditService.class);

        budgetService = new BudgetService(
                budgetRepository,
                apiRepository,
                applicationRepository,
                usageRecordRepository,
                authService,
                auditService
        );
    }

    @Test
    void testBudgetUnderLimitPasses() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");

        Budget b = new Budget();
        b.setPeriodType("DAILY");
        b.setLimitAmount(new BigDecimal("50.0000"));
        b.setBlockingEnabled(true);

        when(budgetRepository.findByApiConfiguration(api)).thenReturn(List.of(b));
        when(usageRecordRepository.sumCostByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(new BigDecimal("25.0000"));

        assertDoesNotThrow(() -> budgetService.checkBudget(api));
    }

    @Test
    void testBudgetExceededThrowsWhenBlockingEnabled() {
        ApiConfiguration api = new ApiConfiguration();
        api.setId(UUID.randomUUID());
        api.setName("Test API");

        Budget b = new Budget();
        b.setPeriodType("DAILY");
        b.setLimitAmount(new BigDecimal("50.0000"));
        b.setBlockingEnabled(true);

        when(budgetRepository.findByApiConfiguration(api)).thenReturn(List.of(b));
        when(usageRecordRepository.sumCostByApiIdSince(eq(api.getId()), any(Instant.class)))
                .thenReturn(new BigDecimal("52.5000"));

        BudgetExceededException ex = assertThrows(
                BudgetExceededException.class,
                () -> budgetService.checkBudget(api)
        );

        assertEquals("BUDGET_EXCEEDED", ex.getCode());
    }
}
