package com.apisentinel.cost;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostCalculator {

    public static final int COST_SCALE = 6;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimal calculateCost(PricingRule pricingRule, int inputUnits, int outputUnits) {
        if (pricingRule == null) {
            return BigDecimal.ZERO.setScale(COST_SCALE, ROUNDING_MODE);
        }

        BigDecimal cost = BigDecimal.ZERO;

        if ("PER_REQUEST".equalsIgnoreCase(pricingRule.getPricingType())) {
            BigDecimal requestPrice = pricingRule.getRequestPrice() != null ? pricingRule.getRequestPrice() : BigDecimal.ZERO;
            cost = cost.add(requestPrice);
        } else if ("PER_UNIT".equalsIgnoreCase(pricingRule.getPricingType())) {
            BigDecimal inputPrice = pricingRule.getInputUnitPrice() != null ? pricingRule.getInputUnitPrice() : BigDecimal.ZERO;
            BigDecimal outputPrice = pricingRule.getOutputUnitPrice() != null ? pricingRule.getOutputUnitPrice() : BigDecimal.ZERO;
            BigDecimal requestPrice = pricingRule.getRequestPrice() != null ? pricingRule.getRequestPrice() : BigDecimal.ZERO;

            BigDecimal inputCost = inputPrice.multiply(BigDecimal.valueOf(Math.max(0, inputUnits)));
            BigDecimal outputCost = outputPrice.multiply(BigDecimal.valueOf(Math.max(0, outputUnits)));

            cost = cost.add(inputCost).add(outputCost).add(requestPrice);
        }

        return cost.setScale(COST_SCALE, ROUNDING_MODE);
    }
}
