package com.ecommerce.user.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.dto.response.SessionResponse;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.models.UserSession;
import com.ecommerce.user.repository.SessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing user sessions.
 * Handles session creation, termination, and listing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    
    private final SessionRepository sessionRepository;
    
    // Session expiration time in days
    private static final int SESSION_EXPIRATION_DAYS = 30;
    
    /**
     * Create a new session for a user
     */
    @Transactional
    public UserSession createSession(String userId, String deviceInfo, String browser, 
                                     String operatingSystem, String ipAddress, String location) {
        // Generate unique session token
        String sessionToken = UUID.randomUUID().toString();
        
        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionToken(sessionToken)
                .deviceInfo(deviceInfo)
                .browser(browser)
                .operatingSystem(operatingSystem)
                .ipAddress(ipAddress)
                .location(location)
                .isCurrent(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(SESSION_EXPIRATION_DAYS))
                .build();
        
        log.info("Creating new session for user: {} from IP: {}", userId, ipAddress);
        return sessionRepository.save(session);
    }
    
    /**
     * Get all active sessions for a user
     */
    public List<SessionResponse> getActiveSessions(String userId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveOrderByLastActivityDesc(userId, true);
        
        return sessions.stream()
                .filter(session -> !session.isExpired())
                .map(session -> mapToResponse(session, currentSessionToken))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all sessions (including inactive) for a user - login history
     */
    public List<SessionResponse> getLoginHistory(String userId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdOrderByLastActivityDesc(userId);
        
        return sessions.stream()
                .map(session -> mapToResponse(session, currentSessionToken))
                .collect(Collectors.toList());
    }
    
    /**
     * Terminate a specific session
     */
    @Transactional
    public void terminateSession(String userId, String sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify session belongs to user
        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session does not belong to user");
        }
        
        session.setActive(false);
        sessionRepository.save(session);
        
        log.info("Terminated session {} for user: {}", sessionId, userId);
    }
    
    /**
     * Terminate all sessions except current
     */
    @Transactional
    public int terminateAllOtherSessions(String userId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveOrderByLastActivityDesc(userId, true);
        
        int count = 0;
        for (UserSession session : sessions) {
            if (!session.getSessionToken().equals(currentSessionToken)) {
                session.setActive(false);
                sessionRepository.save(session);
                count++;
            }
        }
        
        log.info("Terminated {} other sessions for user: {}", count, userId);
        return count;
    }
    
    /**
     * Terminate all sessions for a user (for logout all)
     */
    @Transactional
    public void terminateAllSessions(String userId) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveOrderByLastActivityDesc(userId, true);
        
        for (UserSession session : sessions) {
            session.setActive(false);
            sessionRepository.save(session);
        }
        
        log.info("Terminated all sessions for user: {}", userId);
    }
    
    /**
     * Update session last activity
     */
    @Transactional
    public void updateLastActivity(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
                .ifPresent(session -> {
                    session.setLastActivity(LocalDateTime.now());
                    sessionRepository.save(session);
                });
    }
    
    /**
     * Find session by token
     */
    public UserSession findBySessionToken(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }
    
    /**
     * Get count of active sessions for a user
     */
    public long getActiveSessionCount(String userId) {
        return sessionRepository.countByUserIdAndIsActive(userId, true);
    }
    
    /**
     * Cleanup expired sessions
     */
    @Transactional
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up expired sessions");
    }
    
    /**
     * Delete all sessions for a user (for account deletion)
     */
    @Transactional
    public void deleteAllSessionsForUser(String userId) {
        sessionRepository.deleteByUserId(userId);
        log.info("Deleted all sessions for user: {}", userId);
    }
    
    /**
     * Parse user agent to extract device info
     */
    public DeviceInfo parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return new DeviceInfo("Unknown Device", "Unknown Browser", "Unknown OS");
        }
        
        String browser = "Unknown Browser";
        String os = "Unknown OS";
        String device = "Desktop";
        
        // Detect Browser
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            browser = "Safari";
        } else if (userAgent.contains("Edg")) {
            browser = "Microsoft Edge";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            browser = "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            browser = "Internet Explorer";
        }
        
        // Detect OS
        if (userAgent.contains("Windows NT 10")) {
            os = "Windows 10/11";
        } else if (userAgent.contains("Windows NT 6.3")) {
            os = "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.2")) {
            os = "Windows 8";
        } else if (userAgent.contains("Windows NT 6.1")) {
            os = "Windows 7";
        } else if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            os = "macOS";
        } else if (userAgent.contains("Linux") && userAgent.contains("Android")) {
            os = "Android";
            device = "Mobile";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
            device = userAgent.contains("iPad") ? "Tablet" : "Mobile";
        }
        
        // Detect mobile devices
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || 
            userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            if (!device.equals("Tablet")) {
                device = userAgent.contains("iPad") || userAgent.contains("Tablet") ? "Tablet" : "Mobile";
            }
        }
        
        return new DeviceInfo(device, browser, os);
    }
    
    /**
     * Map UserSession to SessionResponse
     */
    private SessionResponse mapToResponse(UserSession session, String currentSessionToken) {
        boolean isCurrent = session.getSessionToken().equals(currentSessionToken);
        
        return SessionResponse.builder()
                .id(session.getId())
                .deviceInfo(session.getDeviceInfo())
                .browser(session.getBrowser())
                .operatingSystem(session.getOperatingSystem())
                .ipAddress(maskIpAddress(session.getIpAddress()))
                .location(session.getLocation())
                .isCurrent(isCurrent)
                .isActive(session.isActive() && !session.isExpired())
                .createdAt(session.getCreatedAt())
                .lastActivity(session.getLastActivity())
                .build();
    }
    
    /**
     * Mask IP address for privacy (show only first two octets)
     */
    private String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "Unknown";
        }
        
        // Handle IPv4
        if (ipAddress.contains(".")) {
            String[] parts = ipAddress.split("\\.");
            if (parts.length >= 2) {
                return parts[0] + "." + parts[1] + ".*.*";
            }
        }
        
        // Handle IPv6 or localhost
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("::1")) {
            return "localhost";
        }
        
        return ipAddress.substring(0, Math.min(ipAddress.length(), 10)) + "...";
    }
    
    /**
     * Inner class for device info
     */
    public record DeviceInfo(String device, String browser, String operatingSystem) {}
}
