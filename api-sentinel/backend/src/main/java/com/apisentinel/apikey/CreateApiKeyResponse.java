package com.apisentinel.apikey;

import java.time.Instant;
import java.util.UUID;

public record CreateApiKeyResponse(
        UUID id,
        UUID applicationId,
        String keyPrefix,
        String fullSecretKey, // Displayed ONLY once at creation
        String status,
        Instant expiresAt,
        Instant createdAt
) {}
