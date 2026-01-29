package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private String id;
    
    @NotBlank(message = "Label is required")
    @Size(max = 50)
    private String label; // "Home", "Work", "Office"
    
    private Boolean isDefault = false;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;
    
    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;
    
    @NotBlank(message = "Street address is required")
    @Size(max = 200)
    private String street;
    
    // Cambodia-specific fields
    private String village; // ភូមិ
    
    private String commune; // ឃុំ/សង្កាត់
    
    @NotBlank(message = "District is required")
    private String district; // ស្រុក/ខណ្ឌ
    
    @NotBlank(message = "Province is required")
    private String province; // ខេត្ត/រាជធានី
    
    @Size(max = 10)
    private String postalCode;
    
    private String country = "Cambodia";
    
    private String additionalInfo; // Delivery instructions
}
