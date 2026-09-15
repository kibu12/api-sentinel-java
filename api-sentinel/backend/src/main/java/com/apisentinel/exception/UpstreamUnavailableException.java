package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class UpstreamUnavailableException extends SentinelException {
    public UpstreamUnavailableException(String message) {
        super("UPSTREAM_UNAVAILABLE", message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
