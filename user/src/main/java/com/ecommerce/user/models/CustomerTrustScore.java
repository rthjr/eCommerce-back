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
@Document(collection = "customer_trust_scores")
public class CustomerTrustScore {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field(name = "user_id")
    private String userId;

    @Field(name = "score")
    private Double score = 100.0; // Default score: 100

    @Field(name = "cod_limit")
    private Double codLimit = 5000.0; // Default COD limit: 5000

    @Field(name = "total_orders")
    private Integer totalOrders = 0;

    @Field(name = "successful_orders")
    private Integer successfulOrders = 0;

    @Field(name = "failed_orders")
    private Integer failedOrders = 0;

    @Field(name = "cancellations")
    private Integer cancellations = 0;

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
}
