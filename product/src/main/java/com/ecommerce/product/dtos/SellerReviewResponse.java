package com.ecommerce.product.dtos;

import com.ecommerce.product.models.ProductReview.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer rating;
    private String content;
    private Long userId;
    private String userName;
    private LocalDateTime date;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private String sellerResponse;
    private LocalDateTime sellerResponseDate;
    private Boolean isFlagged;
    private String flagReason;
    private ReviewStatus status;
}
