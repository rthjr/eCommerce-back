package com.ecommerce.user.services;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.User;
import com.ecommerce.user.models.UserRole;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
        user.setName(userRequest.getFirstName() + " " + userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        // Note: New User model only has name, email, password, enabled, roles, and timestamps
        // Phone and address fields are not supported in the current User model
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        
        // Split name into firstName and lastName
        String[] nameParts = user.getName().split(" ", 2);
        response.setFirstName(nameParts[0]);
        response.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        response.setEmail(user.getEmail());
        response.setRole(UserRole.CUSTOMER); // Default role for now

        // Set computed fields
        response.setName(user.getName());
        response.setIsAdmin(false); // Default for now
        
        // Note: Address, phone, and avatar fields are not supported in the current User model
        response.setAddress(null);
        
        return response;
    }

    // Delete user
    public boolean deleteUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                }).orElse(false);
    }
}
