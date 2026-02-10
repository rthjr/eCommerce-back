package com.ecommerce.product.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponCodeResponse {
    private Long id;
    private String code;
    private String sellerId;
    private Long campaignId;
    private String campaignName;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer perCustomerLimit;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean isValid;
}
