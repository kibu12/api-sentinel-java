package com.apisentinel.cost;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CostCalculatorTest {

    private CostCalculator costCalculator;

    @BeforeEach
    void setUp() {
        costCalculator = new CostCalculator();
    }

    @Test
    void testRequestPricedApi() {
        PricingRule rule = new PricingRule();
        rule.setPricingType("PER_REQUEST");
        rule.setRequestPrice(new BigDecimal("0.002000"));

        BigDecimal cost = costCalculator.calculateCost(rule, 0, 0);

        assertEquals(new BigDecimal("0.002000"), cost);
    }

    @Test
    void testUnitPricedApiWithTokens() {
        PricingRule rule = new PricingRule();
        rule.setPricingType("PER_UNIT");
        rule.setInputUnitPrice(new BigDecimal("0.000005"));  // $5 per 1M input tokens
        rule.setOutputUnitPrice(new BigDecimal("0.000015")); // $15 per 1M output tokens
        rule.setRequestPrice(new BigDecimal("0.001000"));

        // 1000 input tokens = $0.005, 500 output tokens = $0.0075 + base $0.001 = $0.0135
        BigDecimal cost = costCalculator.calculateCost(rule, 1000, 500);

        assertEquals(new BigDecimal("0.013500"), cost);
    }

    @Test
    void testNullRuleReturnsZero() {
        BigDecimal cost = costCalculator.calculateCost(null, 100, 100);
        assertEquals(new BigDecimal("0.000000"), cost);
    }
}
