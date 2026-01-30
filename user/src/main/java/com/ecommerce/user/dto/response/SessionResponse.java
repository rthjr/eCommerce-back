package com.ecommerce.user.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user session information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    
    private String id;
    
    private String deviceInfo;
    
    private String browser;
    
    private String operatingSystem;
    
    private String ipAddress;
    
    private String location;
    
    private boolean isCurrent;
    
    private boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastActivity;
}
