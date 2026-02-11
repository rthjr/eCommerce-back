package com.ecommerce.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTrustScoreDTO {
    private String id;
    private String userId;
    private Double score;
    private Double codLimit;
    private Integer totalOrders;
    private Integer successfulOrders;
    private Integer failedOrders;
    private Integer cancellations;
    private String createdAt;
    private String updatedAt;
}
