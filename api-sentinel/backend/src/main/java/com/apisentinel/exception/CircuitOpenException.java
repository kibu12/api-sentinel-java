package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class CircuitOpenException extends SentinelException {
    public CircuitOpenException(String message) {
        super("CIRCUIT_OPEN", message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
