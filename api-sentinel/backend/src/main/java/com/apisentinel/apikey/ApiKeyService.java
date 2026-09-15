package com.apisentinel.apikey;

import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.application.ApplicationService;
import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.common.PagedResponse;
import com.apisentinel.exception.ForbiddenException;
import com.apisentinel.exception.ResourceNotFoundException;
import com.apisentinel.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class ApiKeyService {

    private static final String KEY_PREFIX = "sen_live_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final AuthService authService;
    private final AuditService auditService;

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            ApplicationRepository applicationRepository,
            ApplicationService applicationService,
            AuthService authService,
            AuditService auditService) {
        this.apiKeyRepository = apiKeyRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.authService = authService;
        this.auditService = auditService;
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public CreateApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        Application application = applicationService.getApplicationEntity(request.applicationId());
        User currentUser = authService.getCurrentUser();

        byte[] randomBytes = new byte[24];
        RANDOM.nextBytes(randomBytes);
        String secretPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String fullKey = KEY_PREFIX + secretPart;
        String displayPrefix = fullKey.substring(0, Math.min(16, fullKey.length())) + "...";
        String keyHash = hashKey(fullKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setApplication(application);
        apiKey.setKeyPrefix(displayPrefix);
        apiKey.setKeyHash(keyHash);
        apiKey.setStatus("ACTIVE");
        apiKey.setExpiresAt(request.expiresAt());

        apiKey = apiKeyRepository.save(apiKey);

        auditService.record(currentUser, "API_KEY_CREATED", "API_KEY", apiKey.getId().toString(),
                "Created key for app: " + application.getName() + " with prefix: " + displayPrefix);

        return new CreateApiKeyResponse(
                apiKey.getId(),
                application.getId(),
                displayPrefix,
                fullKey,
                apiKey.getStatus(),
                apiKey.getExpiresAt(),
                apiKey.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiKeyDto> getUserApiKeys() {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) return Collections.emptyList();

        return apiKeyRepository.findByApplicationIn(apps).stream()
                .map(ApiKeyDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiKeyDto> getUserApiKeysPaged(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        List<Application> apps = applicationRepository.findByOwner(currentUser);
        if (apps.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), 0, pageable.getPageSize(), 0, 0, true);
        }

        Page<ApiKeyDto> page = apiKeyRepository.findByApplicationIn(apps, pageable)
                .map(ApiKeyDto::from);
        return PagedResponse.from(page);
    }

    @Transactional
    public void revokeApiKey(UUID id) {
        User currentUser = authService.getCurrentUser();
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Key not found: " + id));

        if (!apiKey.getApplication().getOwner().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("You are not authorized to revoke this API key");
        }

        apiKey.setStatus("REVOKED");
        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);

        auditService.record(currentUser, "API_KEY_REVOKED", "API_KEY", id.toString(), "Revoked key: " + apiKey.getKeyPrefix());
    }

    @Transactional
    public ApiKey validateApiKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new UnauthorizedException("API Key is missing");
        }

        String hash = hashKey(rawKey);
        ApiKey apiKey = apiKeyRepository.findByKeyHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid API Key"));

        if (!"ACTIVE".equalsIgnoreCase(apiKey.getStatus())) {
            throw new UnauthorizedException("API Key is " + apiKey.getStatus().toLowerCase());
        }

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("API Key has expired");
        }

        apiKey.setLastUsedAt(Instant.now());
        return apiKeyRepository.save(apiKey);
    }
}
