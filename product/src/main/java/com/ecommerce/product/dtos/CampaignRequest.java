package com.ecommerce.product.dtos;

import com.ecommerce.product.models.Campaign.CampaignType;
import com.ecommerce.product.models.Campaign.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampaignRequest {
    private String name;
    private String description;
    private CampaignType type;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> productIds;
    private Boolean allProducts = false;
    private Integer usageLimit;
    private Integer perCustomerLimit;
}
