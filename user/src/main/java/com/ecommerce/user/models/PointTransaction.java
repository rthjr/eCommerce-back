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
@Document(collection = "point_transactions")
public class PointTransaction {
    @Id
    private String id;

    @Indexed
    @Field(name = "user_id")
    private String userId;

    @Field(name = "points")
    private Integer points;

    @Field(name = "type")
    private String type; // EARN, REDEEM, EXPIRE, BONUS

    @Field(name = "order_id")
    private String orderId;

    @Field(name = "description")
    private String description;

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "expires_at")
    private LocalDateTime expiresAt;
}
