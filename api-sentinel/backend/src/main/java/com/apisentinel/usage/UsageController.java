package com.apisentinel.usage;

import com.apisentinel.common.ApiResponse;
import com.apisentinel.common.PagedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UsageRecordDto>>> getUsageRecords(
            @RequestParam(required = false) UUID apiId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int boundedSize = Math.min(Math.max(1, size), 100);
        PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        PagedResponse<UsageRecordDto> records;
        if (apiId != null) {
            records = usageService.getApiUsageRecords(apiId, pageRequest);
        } else {
            records = usageService.getUserUsageRecords(pageRequest);
        }

        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<UsageSummaryDto>> getSummary() {
        UsageSummaryDto summary = usageService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/cost")
    public ResponseEntity<ApiResponse<List<CostBreakdownDto>>> getCostBreakdown() {
        List<CostBreakdownDto> breakdown = usageService.getCostBreakdown();
        return ResponseEntity.ok(ApiResponse.ok(breakdown));
    }
}
