package com.apisentinel.apikey;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateApiKeyRequest(
        @NotNull(message = "Application ID is required")
        UUID applicationId,

        Instant expiresAt
) {}
