package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class ApiDisabledException extends SentinelException {
    public ApiDisabledException(String message) {
        super("API_DISABLED", message, HttpStatus.FORBIDDEN);
    }
}
