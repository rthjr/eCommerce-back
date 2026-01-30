package com.ecommerce.user.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.dto.DeleteAccountRequest;
import com.ecommerce.user.dto.UserDataExport;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.PasswordResetTokenRepository;
import com.ecommerce.user.repository.RefreshTokenRepository;
import com.ecommerce.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    /**
     * Delete user account with password verification
     */
    @Transactional
    public void deleteAccount(String userId, DeleteAccountRequest request) {
        log.info("Processing account deletion request for user: {}", userId);
        
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        // Verify confirmation
        if (!request.isConfirmDeletion()) {
            throw new IllegalArgumentException("Please confirm account deletion");
        }
        
        String userEmail = user.getEmail();
        String userName = user.getName();
        
        // Delete all user data
        deleteAllUserData(userId);
        
        // Send confirmation email
        emailService.sendAccountDeletionConfirmation(userEmail, userName);
        
        log.info("Account deleted successfully for user: {}", userEmail);
    }
    
    /**
     * Delete all data associated with a user
     */
    private void deleteAllUserData(String userId) {
        // Delete addresses
        addressRepository.deleteByUserId(userId);
        log.debug("Deleted addresses for user: {}", userId);
        
        // Delete refresh tokens
        refreshTokenRepository.deleteByUserId(userId);
        log.debug("Deleted refresh tokens for user: {}", userId);
        
        // Delete password reset tokens
        passwordResetTokenRepository.deleteByUserId(userId);
        log.debug("Deleted password reset tokens for user: {}", userId);
        
        // Delete user
        userRepository.deleteById(userId);
        log.debug("Deleted user: {}", userId);
    }
    
    /**
     * Export all user data (GDPR compliance)
     */
    public UserDataExport exportUserData(String userId) {
        log.info("Exporting data for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get addresses
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        
        // Build export
        return UserDataExport.builder()
                .profile(buildProfileData(user))
                .addresses(buildAddressData(addresses))
                .accountMetadata(buildAccountMetadata(user))
                .exportMetadata(buildExportMetadata(userId))
                .build();
    }
    
    private UserDataExport.ProfileData buildProfileData(User user) {
        return UserDataExport.ProfileData.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    private List<UserDataExport.AddressData> buildAddressData(List<Address> addresses) {
        return addresses.stream()
                .map(addr -> UserDataExport.AddressData.builder()
                        .id(addr.getId())
                        .label(addr.getLabel())
                        .recipientName(addr.getFirstName() + " " + addr.getLastName())
                        .phoneNumber(addr.getPhone())
                        .streetAddress(addr.getStreet())
                        .city(addr.getDistrict()) // Using district as city for Cambodia
                        .province(addr.getProvince())
                        .country(addr.getCountry())
                        .postalCode(addr.getPostalCode())
                        .isDefault(addr.getIsDefault() != null && addr.getIsDefault())
                        .build())
                .collect(Collectors.toList());
    }
    
    private UserDataExport.AccountMetadata buildAccountMetadata(User user) {
        return UserDataExport.AccountMetadata.builder()
                .accountCreated(user.getCreatedAt())
                .lastLogin(null) // TODO: Track last login
                .accountStatus(user.getEnabled() != null && user.getEnabled() ? "ACTIVE" : "DISABLED")
                .emailVerified(false) // TODO: Add email verification
                .twoFactorEnabled(false) // TODO: Add 2FA
                .build();
    }
    
    private UserDataExport.ExportMetadata buildExportMetadata(String userId) {
        return UserDataExport.ExportMetadata.builder()
                .exportedAt(LocalDateTime.now())
                .exportFormat("JSON")
                .exportVersion("1.0")
                .requestedBy(userId)
                .build();
    }
}
