package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;
    
    private String avatar;
}
