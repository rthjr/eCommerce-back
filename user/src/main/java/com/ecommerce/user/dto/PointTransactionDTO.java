package com.ecommerce.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionDTO {
    private String id;
    private String userId;
    private Integer points;
    private String type;
    private String orderId;
    private String description;
    private String createdAt;
    private String expiresAt;
}
