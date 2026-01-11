package com.ecommerce.user.models;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field(name = "token")
    private String token;

    @Field(name = "user_id")
    private String userId;

    @Field(name = "expiry_date")
    private Instant expiryDate;

    @Field(name = "created_at")
    private LocalDateTime createdAt;
}
