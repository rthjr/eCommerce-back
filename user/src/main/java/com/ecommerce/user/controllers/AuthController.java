package com.ecommerce.user.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.dto.request.ForgotPasswordRequest;
import com.ecommerce.user.dto.request.LoginRequest;
import com.ecommerce.user.dto.request.RefreshTokenRequest;
import com.ecommerce.user.dto.request.RegisterRequest;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.dto.request.VerifyResetCodeRequest;
import com.ecommerce.user.dto.response.JwtResponse;
import com.ecommerce.user.dto.response.MessageResponse;
import com.ecommerce.user.dto.response.SessionResponse;
import com.ecommerce.user.security.CustomUserDetailsService.UserPrincipal;
import com.ecommerce.user.services.AuthService;
import com.ecommerce.user.services.PasswordResetService;
import com.ecommerce.user.services.RefreshTokenService;
import com.ecommerce.user.services.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        JwtResponse response = authService.register(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        JwtResponse response = authService.login(loginRequest, userAgent, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        
        JwtResponse response = new JwtResponse();
        response.setAccessToken(newAccessToken);
        response.setTokenType("Bearer");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logoutUser() {
        String message = authService.logout();
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JwtResponse.UserInfo> getCurrentUser() {
        JwtResponse.UserInfo userInfo = authService.getCurrentUser();
        return ResponseEntity.ok(userInfo);
    }
    
    /**
     * Request password reset - sends 6-digit code via email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.processForgotPassword(request);
            // Always return success to prevent email enumeration
            return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, you will receive a 6-digit reset code shortly.",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error processing forgot password request: {}", e.getMessage());
            // Still return success to prevent enumeration
            return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, you will receive a 6-digit reset code shortly.",
                "success", true
            ));
        }
    }
    
    /**
     * Verify 6-digit reset code
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        try {
            boolean isValid = passwordResetService.verifyResetCode(request);
            
            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Reset code is valid"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Invalid or expired reset code"
                ));
            }
        } catch (Exception e) {
            log.error("Error verifying reset code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Invalid or expired reset code"
            ));
        }
    }
    
    /**
     * Reset password using verified 6-digit code
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully",
                "success", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "success", false
            ));
        } catch (com.ecommerce.user.exception.ResourceNotFoundException e) {
            log.warn("Password reset failed - user not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Invalid reset request",
                "success", false
            ));
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Failed to reset password. Please try again.",
                "success", false
            ));
        }
    }
    
    // ============== Session Management Endpoints ==============
    
    /**
     * Get all active sessions for the current user
     */
    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        String userId = getCurrentUserId();
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId, sessionToken);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Get login history for the current user
     */
    @GetMapping("/sessions/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> getLoginHistory(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        String userId = getCurrentUserId();
        List<SessionResponse> sessions = sessionService.getLoginHistory(userId, sessionToken);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Terminate a specific session
     */
    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> terminateSession(@PathVariable String sessionId) {
        try {
            String userId = getCurrentUserId();
            sessionService.terminateSession(userId, sessionId);
            return ResponseEntity.ok(Map.of(
                "message", "Session terminated successfully",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error terminating session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "success", false
            ));
        }
    }
    
    /**
     * Terminate all other sessions except current
     */
    @DeleteMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> terminateAllOtherSessions(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        try {
            String userId = getCurrentUserId();
            int count = sessionService.terminateAllOtherSessions(userId, sessionToken);
            return ResponseEntity.ok(Map.of(
                "message", "Terminated " + count + " sessions",
                "count", count,
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error terminating sessions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "success", false
            ));
        }
    }
    
    /**
     * Get current user ID from security context
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    /**
     * Get client IP address from request, handling proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}
