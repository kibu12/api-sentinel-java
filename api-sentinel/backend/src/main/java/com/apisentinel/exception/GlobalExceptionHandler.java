package com.apisentinel.exception;

import com.apisentinel.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getRequestId(HttpServletRequest request) {
        String reqId = request.getHeader("X-Request-Id");
        if (reqId == null || reqId.isBlank()) {
            reqId = (String) request.getAttribute("requestId");
        }
        if (reqId == null || reqId.isBlank()) {
            reqId = "req-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return reqId;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        log.warn("Rate limit exceeded for request {}: {}", reqId, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of(ex.getCode(), ex.getMessage(), reqId),
                headers,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    @ExceptionHandler(SentinelException.class)
    public ResponseEntity<ErrorResponse> handleSentinelException(SentinelException ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        log.warn("SentinelException [{}]: {} (requestId: {})", ex.getCode(), ex.getMessage(), reqId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of(ex.getCode(), ex.getMessage(), reqId),
                headers,
                ex.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        String message = details.isBlank() ? "Validation failed for request" : details;
        log.warn("Validation error on request {}: {}", reqId, message);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of("VALIDATION_ERROR", message, reqId),
                headers,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        log.warn("Authentication failed for request {}: {}", reqId, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of("UNAUTHORIZED", "Authentication failed: " + ex.getMessage(), reqId),
                headers,
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        log.warn("Access denied for request {}: {}", reqId, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of("FORBIDDEN", "Access denied: insufficient permissions", reqId),
                headers,
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String reqId = getRequestId(request);
        log.error("Unhandled exception for request {}: ", reqId, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", reqId);
        return new ResponseEntity<>(
                ErrorResponse.of("INTERNAL_ERROR", "An unexpected internal server error occurred", reqId),
                headers,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
