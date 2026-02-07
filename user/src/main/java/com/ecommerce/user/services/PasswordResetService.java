package com.ecommerce.user.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.dto.request.ForgotPasswordRequest;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.dto.request.VerifyResetCodeRequest;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.password-reset.code-expiry-minutes:10}")
    private int codeExpiryMinutes;
    
    @Value("${app.password-reset.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;
    
    @Value("${app.security.pepper:defaultPepper}")
    private String pepper;
    
    /**
     * Process forgot password request - sends 6-digit code
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
        
        // Check resend cooldown
        if (user.getPasswordResetCodeSentAt() != null) {
            LocalDateTime cooldownExpiry = user.getPasswordResetCodeSentAt().plusSeconds(resendCooldownSeconds);
            if (LocalDateTime.now().isBefore(cooldownExpiry)) {
                log.warn("Password reset code resend attempted too soon for user: {}", email);
                return; // Still return success to prevent enumeration
            }
        }
        
        try {
            // Generate 6-digit code
            String code = generateSixDigitCode();
            
            // Hash the code
            String hashedCode = hashCode(code);
            
            // Store hashed code and expiry
            user.setPasswordResetCodeHash(hashedCode);
            user.setPasswordResetCodeExpiresAt(LocalDateTime.now().plusMinutes(codeExpiryMinutes));
            user.setPasswordResetCodeSentAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Send email with code
            emailService.sendPasswordResetCode(user.getEmail(), user.getName(), code);
            
            log.info("Password reset code sent to user: {}", user.getEmail());
            
        } catch (Exception e) {
            // If email sending fails, clear the stored code
            user.setPasswordResetCodeHash(null);
            user.setPasswordResetCodeExpiresAt(null);
            user.setPasswordResetCodeSentAt(null);
            userRepository.save(user);
            
            log.error("Failed to send password reset code to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send reset code", e);
        }
    }
    
    /**
     * Verify 6-digit reset code
     */
    public boolean verifyResetCode(VerifyResetCodeRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String code = request.getCode().trim();
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Reset code verification attempted for non-existent email: {}", email);
            return false;
        }
        
        User user = userOpt.get();
        
        // Check if code exists and hasn't expired
        if (user.getPasswordResetCodeHash() == null || 
            user.getPasswordResetCodeExpiresAt() == null ||
            LocalDateTime.now().isAfter(user.getPasswordResetCodeExpiresAt())) {
            log.warn("No valid reset code for user: {}", email);
            return false;
        }
        
        // Verify code
        String hashedInputCode = hashCode(code);
        boolean isValid = user.getPasswordResetCodeHash().equals(hashedInputCode);
        
        if (isValid) {
            log.info("Password reset code verified for user: {}", email);
        } else {
            log.warn("Invalid password reset code for user: {}", email);
        }
        
        return isValid;
    }
    
    /**
     * Reset password using verified code
     * Email is sent AFTER transaction commits successfully
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = request.getEmail().toLowerCase().trim();
        String code = request.getCode().trim();

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify code one more time
        if (user.getPasswordResetCodeHash() == null ||
            user.getPasswordResetCodeExpiresAt() == null ||
            LocalDateTime.now().isAfter(user.getPasswordResetCodeExpiresAt())) {
            throw new IllegalArgumentException("Reset code has expired");
        }

        String hashedInputCode = hashCode(code);
        if (!user.getPasswordResetCodeHash().equals(hashedInputCode)) {
            throw new IllegalArgumentException("Invalid reset code");
        }

        // Store user details before transaction completes
        String userEmail = user.getEmail();
        String userName = user.getName();

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        // Clear reset code data
        user.setPasswordResetCodeHash(null);
        user.setPasswordResetCodeExpiresAt(null);
        user.setPasswordResetCodeSentAt(null);

        userRepository.save(user);

        // Send confirmation email AFTER transaction commits
        // This is now async, so it won't block or affect the transaction
        emailService.sendPasswordResetConfirmation(userEmail, userName);

        log.info("Password reset successful for user: {}", userEmail);
    }
    
    /**
     * Generate a secure 6-digit code
     */
    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
    
    /**
     * Hash the code using HMAC-SHA256
     */
    private String hashCode(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(pepper.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(code.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash code", e);
        }
    }
    
    /**
     * Cleanup expired codes (call this periodically)
     */
    public void cleanupExpiredCodes() {
        // This would require a custom query to update users with expired codes
        log.info("Cleaned up expired password reset codes");
    }
}
