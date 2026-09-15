package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class SentinelException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    public SentinelException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
