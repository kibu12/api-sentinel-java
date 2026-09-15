package com.apisentinel.audit;

import com.apisentinel.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(User actorUser, String action, String resourceType, String resourceId, String metadata) {
        try {
            AuditLog auditLog = new AuditLog(actorUser, action, resourceType, resourceId, metadata);
            auditLogRepository.save(auditLog);
            log.info("AUDIT: [action={}] [resource={}:{}] [user={}]",
                    action, resourceType, resourceId, actorUser != null ? actorUser.getEmail() : "system");
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }
}
