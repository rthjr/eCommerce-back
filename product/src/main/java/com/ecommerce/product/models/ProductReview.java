package com.ecommerce.product.models;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rating;
    private String content;
    private Long userId;
    private String user;
    @CreationTimestamp
    private LocalDateTime date;
    private Boolean verifiedPurchase = false;
    private Integer helpfulCount = 0;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)    
    private Product product;
    
    // Seller feedback management fields
    @Column(columnDefinition = "TEXT")
    private String sellerResponse;
    
    private LocalDateTime sellerResponseDate;
    
    private Boolean isFlagged = false;
    
    private String flagReason;
    
    private LocalDateTime flaggedAt;
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PUBLISHED;
    
    public enum ReviewStatus {
        PUBLISHED,
        HIDDEN,
        FLAGGED,
        UNDER_REVIEW
    }
}
