package com.ecommerce.product.dtos;

import com.ecommerce.product.models.Campaign.CampaignStatus;
import com.ecommerce.product.models.Campaign.CampaignType;
import com.ecommerce.product.models.Campaign.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampaignResponse {
    private Long id;
    private String sellerId;
    private String name;
    private String description;
    private CampaignType type;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CampaignStatus status;
    private Boolean isActive;
    private List<Long> productIds;
    private Boolean allProducts;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer perCustomerLimit;
    private Integer views;
    private Integer clicks;
    private Integer conversions;
    private BigDecimal revenueGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
