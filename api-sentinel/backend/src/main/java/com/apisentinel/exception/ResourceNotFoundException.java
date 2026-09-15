package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SentinelException {
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
