package com.apisentinel.exception;

import org.springframework.http.HttpStatus;

public class BudgetExceededException extends SentinelException {
    public BudgetExceededException(String message) {
        super("BUDGET_EXCEEDED", message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
