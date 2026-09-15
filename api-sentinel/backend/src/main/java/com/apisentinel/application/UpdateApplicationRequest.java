package com.apisentinel.application;

import jakarta.validation.constraints.Size;

public record UpdateApplicationRequest(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        String environment,
        String status // "ACTIVE", "DISABLED"
) {}
