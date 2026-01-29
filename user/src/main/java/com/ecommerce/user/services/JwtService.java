package com.ecommerce.user.services;

import org.springframework.stereotype.Service;

import com.ecommerce.user.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service wrapper for JWT operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Extract user ID from JWT token
     */
    public String extractUserId(String token) {
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
    
    /**
     * Extract email from JWT token
     */
    public String extractEmail(String token) {
        return jwtTokenProvider.getEmailFromJWT(token);
    }
    
    /**
     * Extract roles from JWT token
     */
    public String extractRoles(String token) {
        return jwtTokenProvider.getRolesFromJWT(token);
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
