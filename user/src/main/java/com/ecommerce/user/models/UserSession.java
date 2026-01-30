package com.ecommerce.user.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Represents an active user session for session management.
 * Tracks login history and allows users to manage their active sessions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_sessions")
public class UserSession {
    
    @Id
    private String id;
    
    @Indexed
    @Field(name = "user_id")
    private String userId;
    
    @Indexed
    @Field(name = "session_token")
    private String sessionToken;
    
    @Field(name = "device_info")
    private String deviceInfo;
    
    @Field(name = "browser")
    private String browser;
    
    @Field(name = "operating_system")
    private String operatingSystem;
    
    @Field(name = "ip_address")
    private String ipAddress;
    
    @Field(name = "location")
    private String location;
    
    @Field(name = "is_current")
    private boolean isCurrent;
    
    @Field(name = "created_at")
    private LocalDateTime createdAt;
    
    @Field(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Field(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Field(name = "is_active")
    private boolean isActive;
    
    /**
     * Check if the session has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
