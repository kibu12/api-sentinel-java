package com.apisentinel.anomaly;

import com.apisentinel.common.ApiResponse;
import com.apisentinel.common.PagedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anomalies")
public class AnomalyController {

    private final AnomalyService anomalyService;

    public AnomalyController(AnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AnomalyDto>>> getAnomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int boundedSize = Math.min(Math.max(1, size), 100);
        PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "detectedAt"));
        PagedResponse<AnomalyDto> response = anomalyService.getUserAnomalies(pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AnomalyDto>> resolveAnomaly(@PathVariable UUID id) {
        AnomalyDto resolved = anomalyService.resolveAnomaly(id);
        return ResponseEntity.ok(ApiResponse.ok(resolved));
    }
}
