package com.apisentinel.apikey;

import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.application.ApplicationService;
import com.apisentinel.audit.AuditService;
import com.apisentinel.auth.AuthService;
import com.apisentinel.auth.User;
import com.apisentinel.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ApiKeyServiceTest {

    private ApiKeyRepository apiKeyRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationService applicationService;
    private AuthService authService;
    private AuditService auditService;
    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyRepository = Mockito.mock(ApiKeyRepository.class);
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        applicationService = Mockito.mock(ApplicationService.class);
        authService = Mockito.mock(AuthService.class);
        auditService = Mockito.mock(AuditService.class);

        apiKeyService = new ApiKeyService(
                apiKeyRepository,
                applicationRepository,
                applicationService,
                authService,
                auditService
        );
    }

    @Test
    void testCreateApiKeyReturnsFullKeyOnce() {
        UUID appId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setName("App");

        User user = new User();
        user.setId(UUID.randomUUID());

        when(applicationService.getApplicationEntity(appId)).thenReturn(app);
        when(authService.getCurrentUser()).thenReturn(user);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(i -> {
            ApiKey k = i.getArgument(0);
            k.setId(UUID.randomUUID());
            return k;
        });

        CreateApiKeyResponse response = apiKeyService.createApiKey(new CreateApiKeyRequest(appId, null));

        assertNotNull(response.fullSecretKey());
        assertTrue(response.fullSecretKey().startsWith("sen_live_"));
        assertTrue(response.keyPrefix().startsWith("sen_live_"));
    }

    @Test
    void testValidateApiKeyThrowsOnInvalid() {
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> apiKeyService.validateApiKey("invalid_raw_key"));
    }
}
