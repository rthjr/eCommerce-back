package com.ecommerce.user.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    @Field(name = "name")
    private String name;

    @Email
    @NotBlank
    @Size(max = 100)
    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    @NotBlank
    @Size(min = 6)
    private String password;

    @Field(name = "phone")
    @Size(max = 20)
    private String phone;

    @Field(name = "avatar")
    private String avatar;

    @Field(name = "enabled")
    private Boolean enabled = true;

    @Field(name = "roles")
    private Set<String> roles = new HashSet<>();

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Password reset fields
    @JsonIgnore
    @Field(name = "password_reset_code_hash")
    private String passwordResetCodeHash;
    
    @JsonIgnore
    @Field(name = "password_reset_code_expires_at")
    private LocalDateTime passwordResetCodeExpiresAt;
    
    @JsonIgnore
    @Field(name = "password_reset_code_sent_at")
    private LocalDateTime passwordResetCodeSentAt;
}
