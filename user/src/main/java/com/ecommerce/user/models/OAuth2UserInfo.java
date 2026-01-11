package com.ecommerce.user.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "oauth2_user_info")
@Data
@NoArgsConstructor
public class OAuth2UserInfo {
    
    @Id
    private String id;
    
    @Field("user_id")
    @Indexed
    private String userId;
    
    @Field("provider")
    @Indexed
    private String provider;
    
    @Field("provider_id")
    @Indexed
    private String providerId;
    
    @Field("email")
    private String email;
    
    @Field("name")
    private String name;
    
    @Field("avatar_url")
    private String avatarUrl;
    
    @Field("attributes")
    private String attributes;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}