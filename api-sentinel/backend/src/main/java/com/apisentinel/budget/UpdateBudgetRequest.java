package com.apisentinel.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record UpdateBudgetRequest(
        String periodType, // "DAILY" or "MONTHLY"

        @DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
        BigDecimal limitAmount,

        @Min(1) @Max(100)
        Integer warningPercent,

        @Min(1) @Max(100)
        Integer criticalPercent,

        Boolean blockingEnabled
) {}
