package com.ecommerce.correlation.exception;

public class BackendQueryException extends RuntimeException {
    private final String backend;

    public BackendQueryException(String backend, String message, Throwable cause) {
        super(message, cause);
        this.backend = backend;
    }

    public BackendQueryException(String backend, String message) {
        super(message);
        this.backend = backend;
    }

    public String getBackend() {
        return backend;
    }
}
