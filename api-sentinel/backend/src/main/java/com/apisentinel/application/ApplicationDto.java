package com.apisentinel.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationDto(
        UUID id,
        UUID ownerId,
        String name,
        String environment,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ApplicationDto from(Application app) {
        return new ApplicationDto(
                app.getId(),
                app.getOwner().getId(),
                app.getName(),
                app.getEnvironment(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}
