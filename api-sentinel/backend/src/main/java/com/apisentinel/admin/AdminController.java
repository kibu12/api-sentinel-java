package com.apisentinel.admin;

import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.audit.AuditLog;
import com.apisentinel.audit.AuditLogRepository;
import com.apisentinel.auth.UserRepository;
import com.apisentinel.common.ApiResponse;
import com.apisentinel.common.PagedResponse;
import com.apisentinel.usage.UsageRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApiConfigurationRepository apiRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(
            UserRepository userRepository,
            ApplicationRepository applicationRepository,
            ApiConfigurationRepository apiRepository,
            UsageRecordRepository usageRecordRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.apiRepository = apiRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformStats() {
        long userCount = userRepository.count();
        long appCount = applicationRepository.count();
        long apiCount = apiRepository.count();
        long totalUsage = usageRecordRepository.count();

        Map<String, Object> stats = Map.of(
                "totalUsers", userCount,
                "totalApplications", appCount,
                "totalApis", apiCount,
                "totalUsageRecords", totalUsage
        );

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int boundedSize = Math.min(Math.max(1, size), 100);
        PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(logs)));
    }
}
