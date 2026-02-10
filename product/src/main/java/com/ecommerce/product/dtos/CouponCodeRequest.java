package com.ecommerce.product.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponCodeRequest {
    private String code; // Optional - will be auto-generated if not provided
    private Long campaignId;
    private Integer usageLimit;
    private Integer perCustomerLimit;
    private LocalDateTime expiresAt;
}
