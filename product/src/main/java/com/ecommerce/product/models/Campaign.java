package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "campaigns")
@Data
@NoArgsConstructor
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerId;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private CampaignType type = CampaignType.PERCENTAGE_DISCOUNT;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType = DiscountType.PERCENTAGE;

    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status = CampaignStatus.DRAFT;

    private Boolean isActive = false;

    // Products included in campaign (could be all products or specific ones)
    @ElementCollection
    @CollectionTable(name = "campaign_products", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "product_id")
    private List<Long> productIds = new ArrayList<>();

    private Boolean allProducts = false; // If true, applies to all seller's products

    // Usage limits
    private Integer usageLimit;
    private Integer usageCount = 0;
    private Integer perCustomerLimit;

    // Performance tracking
    private Integer views = 0;
    private Integer clicks = 0;
    private Integer conversions = 0;
    private BigDecimal revenueGenerated = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum CampaignType {
        PERCENTAGE_DISCOUNT,  // 10% off
        FIXED_DISCOUNT,       // $5 off
        BOGO,                 // Buy One Get One
        BUNDLE,               // Bundle deals
        FLASH_SALE,           // Limited time
        FREE_SHIPPING         // Free shipping
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    public enum CampaignStatus {
        DRAFT,
        SCHEDULED,
        ACTIVE,
        PAUSED,
        ENDED,
        CANCELLED
    }
}
