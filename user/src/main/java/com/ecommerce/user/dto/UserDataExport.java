package com.ecommerce.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for user data export (GDPR compliance)
 */
@Data
@Builder
public class UserDataExport {
    
    // Profile information
    private ProfileData profile;
    
    // Addresses
    private List<AddressData> addresses;
    
    // Account metadata
    private AccountMetadata accountMetadata;
    
    // Export metadata
    private ExportMetadata exportMetadata;
    
    @Data
    @Builder
    public static class ProfileData {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String avatar;
        private List<String> roles;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    public static class AddressData {
        private String id;
        private String label;
        private String recipientName;
        private String phoneNumber;
        private String streetAddress;
        private String city;
        private String province;
        private String country;
        private String postalCode;
        private boolean isDefault;
    }
    
    @Data
    @Builder
    public static class AccountMetadata {
        private LocalDateTime accountCreated;
        private LocalDateTime lastLogin;
        private String accountStatus;
        private boolean emailVerified;
        private boolean twoFactorEnabled;
    }
    
    @Data
    @Builder
    public static class ExportMetadata {
        private LocalDateTime exportedAt;
        private String exportFormat;
        private String exportVersion;
        private String requestedBy;
    }
}
