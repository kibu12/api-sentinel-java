package com.apisentinel.auth;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String role,
        String status,
        Instant createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
