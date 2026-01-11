package com.ecommerce.user.models;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "oauth2_tokens")
@Data
@NoArgsConstructor
public class OAuth2Token {
    
    @Id
    private String id;
    
    private String userId;
    private String provider;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String scope;
    private Instant expiresAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
}