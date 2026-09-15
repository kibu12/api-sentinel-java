package com.apisentinel.apikey;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyDto(
        UUID id,
        UUID applicationId,
        String applicationName,
        String keyPrefix,
        String status,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant createdAt,
        Instant revokedAt
) {
    public static ApiKeyDto from(ApiKey apiKey) {
        return new ApiKeyDto(
                apiKey.getId(),
                apiKey.getApplication().getId(),
                apiKey.getApplication().getName(),
                apiKey.getKeyPrefix(),
                apiKey.getStatus(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getCreatedAt(),
                apiKey.getRevokedAt()
        );
    }
}
