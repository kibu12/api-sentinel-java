package com.apisentinel.common;

import java.time.Instant;

public record ErrorResponse(
        boolean success,
        ErrorInfo error,
        String timestamp,
        String requestId
) {
    public record ErrorInfo(String code, String message) {}

    public static ErrorResponse of(String code, String message, String requestId) {
        return new ErrorResponse(false, new ErrorInfo(code, message), Instant.now().toString(), requestId);
    }
}
