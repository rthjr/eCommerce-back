package com.ecommerce.correlation.controller;

import com.ecommerce.correlation.exception.BackendQueryException;
import com.ecommerce.correlation.exception.CorrelationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CorrelationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CorrelationNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BackendQueryException.class)
    public ResponseEntity<Map<String, Object>> handleBackendFailure(BackendQueryException ex) {
        return error(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        String safeMessage = message == null ? status.getReasonPhrase() : message;
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", safeMessage
        ));
    }
}
