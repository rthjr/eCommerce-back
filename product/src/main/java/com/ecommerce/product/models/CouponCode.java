package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "coupon_codes")
@Data
@NoArgsConstructor
public class CouponCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    private String sellerId;

    private Integer usageLimit;
    private Integer usageCount = 0;
    private Integer perCustomerLimit;

    private Boolean isActive = true;

    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isValid() {
        if (!isActive) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (usageLimit != null && usageCount >= usageLimit) return false;
        return true;
    }
}
