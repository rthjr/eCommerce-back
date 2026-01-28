package com.ecommerce.user.utils;

import java.time.LocalDateTime;
import java.util.Set;

import com.ecommerce.user.models.User;
import com.ecommerce.user.models.UserRole;

public class StaticUsers {
    
    public static final User ADMIN_USER = createUser(
        "admin@ecommerce.com",
        "Admin User", 
        "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HI/2Cmdpj77Mu.khGqaDa", // password123
        Set.of(UserRole.ROLE_ADMIN.name())
    );
    
    public static final User REGULAR_USER = createUser(
        "user@ecommerce.com",
        "Regular User",
        "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HI/2Cmdpj77Mu.khGqaDa", // password123
        Set.of(UserRole.ROLE_USER.name())
    );
    
    public static final User CUSTOMER_USER = createUser(
        "customer@ecommerce.com", 
        "Customer User",
        "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HI/2Cmdpj77Mu.khGqaDa", // password123
        Set.of(UserRole.ROLE_CUSTOMER.name())
    );
    
    private static User createUser(String email, String name, String password, Set<String> roles) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setRoles(roles);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}