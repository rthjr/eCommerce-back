package com.ecommerce.correlation.exception;

public class CorrelationNotFoundException extends RuntimeException {
    public CorrelationNotFoundException(String message) {
        super(message);
    }
}
