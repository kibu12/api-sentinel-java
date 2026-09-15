package com.apisentinel.budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetDto(
        UUID id,
        UUID apiId,
        String apiName,
        String periodType,
        BigDecimal limitAmount,
        BigDecimal currentSpend,
        double percentageUsed,
        int warningPercent,
        int criticalPercent,
        boolean blockingEnabled,
        String status, // "NORMAL", "WARNING", "CRITICAL", "EXCEEDED"
        Instant currentPeriodStart,
        Instant updatedAt
) {
    public static BudgetDto from(Budget budget, BigDecimal currentSpend) {
        BigDecimal spend = currentSpend != null ? currentSpend : BigDecimal.ZERO;
        double pct = 0.0;
        if (budget.getLimitAmount() != null && budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            pct = spend.divide(budget.getLimitAmount(), 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        String status = "NORMAL";
        if (pct >= 100.0) {
            status = "EXCEEDED";
        } else if (pct >= budget.getCriticalPercent()) {
            status = "CRITICAL";
        } else if (pct >= budget.getWarningPercent()) {
            status = "WARNING";
        }

        return new BudgetDto(
                budget.getId(),
                budget.getApiConfiguration().getId(),
                budget.getApiConfiguration().getName(),
                budget.getPeriodType(),
                budget.getLimitAmount(),
                spend,
                Math.round(pct * 100.0) / 100.0,
                budget.getWarningPercent(),
                budget.getCriticalPercent(),
                budget.isBlockingEnabled(),
                status,
                budget.getCurrentPeriodStart(),
                budget.getUpdatedAt()
        );
    }
}
