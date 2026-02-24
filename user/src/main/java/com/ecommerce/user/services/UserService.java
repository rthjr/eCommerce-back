package com.ecommerce.user.services;

import com.ecommerce.user.dto.PasswordChangeRequest;
import com.ecommerce.user.dto.ProfileUpdateRequest;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.User;
import com.ecommerce.user.models.UserRole;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser(UserRequest userRequest) {
        User user = new User();
        updateUserFromRequest(user, userRequest);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    public boolean updateUser(String id, UserRequest updatedUserRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    updateUserFromRequest(existingUser, updatedUserRequest);
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);
    }

    private void updateUserFromRequest(User user, UserRequest userRequest) {
        String firstName = userRequest.getFirstName();
        String lastName = userRequest.getLastName();
        if (firstName != null || lastName != null) {
            String fullName = String.format("%s %s",
                    firstName != null ? firstName.trim() : "",
                    lastName != null ? lastName.trim() : "")
                    .trim();
            if (!fullName.isEmpty()) {
                user.setName(fullName);
            }
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPhone() != null) {
            user.setPhone(userRequest.getPhone());
        }
        if (userRequest.getRole() != null) {
            user.setRoles(new HashSet<>(Set.of(userRequest.getRole().name())));
        } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(Set.of(UserRole.ROLE_CUSTOMER.name())));
        }
        user.setUpdatedAt(LocalDateTime.now());
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        
        // Split name into firstName and lastName
        String name = user.getName() != null ? user.getName() : "";
        String[] nameParts = name.split(" ", 2);
        response.setFirstName(nameParts[0]);
        response.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        UserRole resolvedRole = resolvePrimaryRole(user.getRoles());
        response.setRole(resolvedRole);

        // Set computed fields
        response.setName(name);
        response.setIsAdmin(resolvedRole == UserRole.ROLE_ADMIN);
        
        // Address is now managed separately via AddressController
        response.setAddress(null);
        
        return response;
    }

    private UserRole resolvePrimaryRole(Set<String> roles) {
        Set<String> safeRoles = roles != null ? roles : Collections.emptySet();

        if (safeRoles.contains(UserRole.ROLE_ADMIN.name())) {
            return UserRole.ROLE_ADMIN;
        }
        if (safeRoles.contains(UserRole.ROLE_USER.name())) {
            return UserRole.ROLE_USER;
        }
        if (safeRoles.contains(UserRole.ROLE_CUSTOMER.name())) {
            return UserRole.ROLE_CUSTOMER;
        }

        return UserRole.ROLE_CUSTOMER;
    }

    // Delete user
    public boolean deleteUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                }).orElse(false);
    }
    
    /**
     * Update user profile
     */
    public Optional<UserResponse> updateProfile(String userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        return userRepository.findById(userId)
                .map(user -> {
                    // Update name if firstName or lastName provided
                    if (request.getFirstName() != null || request.getLastName() != null) {
                        String firstName = request.getFirstName() != null ? 
                            request.getFirstName() : 
                            (user.getName() != null ? user.getName().split(" ", 2)[0] : "");
                        String lastName = request.getLastName() != null ? 
                            request.getLastName() : 
                            (user.getName() != null && user.getName().split(" ", 2).length > 1 ? 
                                user.getName().split(" ", 2)[1] : "");
                        user.setName(firstName + " " + lastName);
                    }
                    
                    // Update email if provided
                    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                        // Check if email is already taken
                        if (userRepository.existsByEmail(request.getEmail())) {
                            throw new RuntimeException("Email already in use");
                        }
                        user.setEmail(request.getEmail());
                    }
                    
                    // Update phone if provided
                    if (request.getPhone() != null) {
                        user.setPhone(request.getPhone());
                    }
                    
                    // Update avatar if provided
                    if (request.getAvatar() != null) {
                        user.setAvatar(request.getAvatar());
                    }
                    
                    user.setUpdatedAt(LocalDateTime.now());
                    User savedUser = userRepository.save(user);
                    
                    log.info("Profile updated for user: {}", userId);
                    return mapToUserResponse(savedUser);
                });
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(String userId, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", userId);
        
        return userRepository.findById(userId)
                .map(user -> {
                    // Verify current password
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        log.warn("Current password does not match for user: {}", userId);
                        throw new RuntimeException("Current password is incorrect");
                    }
                    
                    // Verify new password matches confirmation
                    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                        throw new RuntimeException("New password and confirmation do not match");
                    }
                    
                    // Update password
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    
                    log.info("Password changed for user: {}", userId);
                    return true;
                }).orElse(false);
    }
    
    /**
     * Get user by ID (internal use)
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
}
