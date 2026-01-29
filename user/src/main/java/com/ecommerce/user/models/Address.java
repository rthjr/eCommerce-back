package com.ecommerce.user.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Document(collection = "addresses")
public class Address {
    @Id
    private String id;

    @NotBlank
    @Indexed
    @Field(name = "user_id")
    private String userId;

    @NotBlank
    @Size(max = 50)
    @Field(name = "label")
    private String label; // "Home", "Work", "Office", etc.

    @Field(name = "is_default")
    private Boolean isDefault = false;

    @NotBlank
    @Size(max = 50)
    @Field(name = "first_name")
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Field(name = "last_name")
    private String lastName;

    @NotBlank
    @Size(max = 20)
    @Field(name = "phone")
    private String phone;

    @NotBlank
    @Size(max = 200)
    @Field(name = "street")
    private String street;

    // Cambodia-specific fields
    @Field(name = "village")
    private String village; // ភូមិ

    @Field(name = "commune")
    private String commune; // ឃុំ/សង្កាត់

    @NotBlank
    @Field(name = "district")
    private String district; // ស្រុក/ខណ្ឌ

    @NotBlank
    @Field(name = "province")
    private String province; // ខេត្ត/រាជធានី

    @Field(name = "postal_code")
    @Size(max = 10)
    private String postalCode;

    @Field(name = "country")
    private String country = "Cambodia"; // Default to Cambodia

    @Field(name = "additional_info")
    private String additionalInfo; // Delivery instructions, landmarks, etc.

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
}
