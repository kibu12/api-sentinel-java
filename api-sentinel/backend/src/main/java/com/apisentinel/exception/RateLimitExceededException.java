package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends SentinelException {
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super("RATE_LIMIT_EXCEEDED", message, HttpStatus.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(String message) {
        this(message, 1);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
