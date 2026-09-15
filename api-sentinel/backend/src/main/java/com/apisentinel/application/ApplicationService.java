package com.apisentinel.application;

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

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            AuthService authService,
            AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional
    public ApplicationDto createApplication(CreateApplicationRequest request) {
        User currentUser = authService.getCurrentUser();

        Application app = new Application();
        app.setOwner(currentUser);
        app.setName(request.name().trim());
        app.setEnvironment(request.environment() != null ? request.environment() : "DEVELOPMENT");
        app.setStatus("ACTIVE");

        app = applicationRepository.save(app);
        auditService.record(currentUser, "APPLICATION_CREATED", "APPLICATION", app.getId().toString(), "Created app: " + app.getName());

        return ApplicationDto.from(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getUserApplications() {
        User currentUser = authService.getCurrentUser();
        return applicationRepository.findByOwner(currentUser).stream()
                .map(ApplicationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationDto> getUserApplicationsPaged(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<ApplicationDto> page = applicationRepository.findByOwner(currentUser, pageable)
                .map(ApplicationDto::from);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(UUID id) {
        Application app = getApplicationEntity(id);
        return ApplicationDto.from(app);
    }

    @Transactional
    public ApplicationDto updateApplication(UUID id, UpdateApplicationRequest request) {
        Application app = getApplicationEntity(id);
        User currentUser = authService.getCurrentUser();

        if (request.name() != null && !request.name().isBlank()) {
            app.setName(request.name().trim());
        }
        if (request.environment() != null && !request.environment().isBlank()) {
            app.setEnvironment(request.environment().trim());
        }
        if (request.status() != null && !request.status().isBlank()) {
            app.setStatus(request.status().trim());
        }

        app = applicationRepository.save(app);
        auditService.record(currentUser, "APPLICATION_UPDATED", "APPLICATION", app.getId().toString(), "Updated status/name");

        return ApplicationDto.from(app);
    }

    @Transactional
    public void deleteApplication(UUID id) {
        Application app = getApplicationEntity(id);
        User currentUser = authService.getCurrentUser();

        applicationRepository.delete(app);
        auditService.record(currentUser, "APPLICATION_DELETED", "APPLICATION", id.toString(), "Deleted app: " + app.getName());
    }

    public Application getApplicationEntity(UUID id) {
        User currentUser = authService.getCurrentUser();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        // Enforce strict multi-tenant authorization rule (AC-02)
        if (!app.getOwner().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You are not authorized to access this application");
        }

        return app;
    }
}
