package com.apisentinel.anomaly;

import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.common.PagedResponse;
import com.apisentinel.exception.ForbiddenException;
import com.apisentinel.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AnomalyService {

    private final AnomalyRepository anomalyRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public AnomalyService(AnomalyRepository anomalyRepository, AuthService authService, AuditService auditService) {
        this.anomalyRepository = anomalyRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnomalyDto> getUserAnomalies(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<AnomalyDto> page = anomalyRepository.findByOwnerId(currentUser.getId(), pageable)
                .map(AnomalyDto::from);
        return PagedResponse.from(page);
    }

    @Transactional
    public AnomalyDto resolveAnomaly(UUID id) {
        User currentUser = authService.getCurrentUser();
        Anomaly anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly not found: " + id));

        if (!anomaly.getApiConfiguration().getApplication().getOwner().getId().equals(currentUser.getId()) &&
                !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You are not authorized to resolve this anomaly");
        }

        anomaly.setStatus("RESOLVED");
        anomaly.setResolvedAt(Instant.now());
        anomaly = anomalyRepository.save(anomaly);

        auditService.record(currentUser, "ANOMALY_RESOLVED", "ANOMALY", id.toString(),
                "Resolved anomaly type: " + anomaly.getType());

        return AnomalyDto.from(anomaly);
    }
}
