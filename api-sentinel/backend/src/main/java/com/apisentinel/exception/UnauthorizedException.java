package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends SentinelException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
