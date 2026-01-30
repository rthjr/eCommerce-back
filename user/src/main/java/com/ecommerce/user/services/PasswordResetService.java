package com.ecommerce.user.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.user.dto.request.ForgotPasswordRequest;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.models.PasswordResetToken;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.PasswordResetTokenRepository;
import com.ecommerce.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.password-reset.token-expiry-hours:1}")
    private int tokenExpiryHours;
    
    /**
     * Process forgot password request
     * Generates a reset token and sends email
     */
    public void processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // Always return success to prevent email enumeration attacks
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        User user = userOpt.get();
        
        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());
        
        // Generate new reset token
        String token = generateResetToken();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(resetToken);
        
        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        
        log.info("Password reset token generated for user: {}", user.getEmail());
    }
    
    /**
     * Validate reset token
     */
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            log.warn("Invalid password reset token: {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (!resetToken.isValid()) {
            log.warn("Expired or used password reset token: {}", token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Reset password using token
     */
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        // Find and validate token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));
        
        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Reset token has expired or already been used");
        }
        
        // Find user
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        // Send confirmation email
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getName());
        
        log.info("Password reset successful for user: {}", user.getEmail());
    }
    
    /**
     * Generate a secure reset token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * Cleanup expired tokens (call this periodically)
     */
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }
}
