package com.apisentinel.auth;

public record AuthResponse(
        String token,
        UserDto user
) {}
