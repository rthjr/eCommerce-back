package com.ecommerce.user.models;

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
@Document(collection = "loyalty_accounts")
public class LoyaltyAccount {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field(name = "user_id")
    private String userId;

    @Field(name = "total_points")
    private Integer totalPoints = 0;

    @Field(name = "current_points")
    private Integer currentPoints = 0;

    @Field(name = "tier")
    private String tier = "BRONZE"; // BRONZE, SILVER, GOLD, PLATINUM

    @Field(name = "tier_updated_at")
    private LocalDateTime tierUpdatedAt;

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
}
