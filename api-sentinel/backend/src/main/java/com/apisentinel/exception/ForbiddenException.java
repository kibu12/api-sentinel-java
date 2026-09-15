package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends SentinelException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }
}
