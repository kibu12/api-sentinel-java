package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class UpstreamTimeoutException extends SentinelException {
    public UpstreamTimeoutException(String message) {
        super("UPSTREAM_TIMEOUT", message, HttpStatus.GATEWAY_TIMEOUT);
    }
}
