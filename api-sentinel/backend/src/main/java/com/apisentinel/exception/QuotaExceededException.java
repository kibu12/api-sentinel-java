package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class QuotaExceededException extends SentinelException {
    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
