package com.ecommerce.order.dtos;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private AddressDTO address;

    // Computed fields for frontend compatibility
    private String name; // firstName + lastName
    private Boolean isAdmin; // role == ADMIN
    private String avatar;
}
