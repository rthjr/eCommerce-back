package com.ecommerce.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OAuth2AuthenticationException extends RuntimeException {
    
    public OAuth2AuthenticationException(String message) {
        super(message);
    }
    
    public OAuth2AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
