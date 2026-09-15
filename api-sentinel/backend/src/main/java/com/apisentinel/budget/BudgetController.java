package com.apisentinel.budget;

import com.apisentinel.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDto>>> getUserBudgets() {
        List<BudgetDto> budgets = budgetService.getUserBudgets();
        return ResponseEntity.ok(ApiResponse.ok(budgets));
    }

    @PutMapping("/{apiId}")
    public ResponseEntity<ApiResponse<List<BudgetDto>>> updateBudget(
            @PathVariable UUID apiId,
            @Valid @RequestBody UpdateBudgetRequest request) {
        List<BudgetDto> updated = budgetService.updateApiBudgets(apiId, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
