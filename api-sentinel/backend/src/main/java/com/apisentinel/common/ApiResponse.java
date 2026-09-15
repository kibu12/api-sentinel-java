package com.apisentinel.common;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        T data,
        String timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, Instant.now().toString());
    }

    public static <T> ApiResponse<T> empty() {
        return new ApiResponse<>(true, null, Instant.now().toString());
    }
}
