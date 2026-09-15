package com.apisentinel.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotBlank(message = "Application name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        String environment // "DEVELOPMENT", "STAGING", "PRODUCTION"
) {}
