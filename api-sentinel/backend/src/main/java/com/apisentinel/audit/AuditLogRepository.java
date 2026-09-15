package com.apisentinel.audit;

import com.apisentinel.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByActorUserOrderByCreatedAtDesc(User actorUser, Pageable pageable);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
