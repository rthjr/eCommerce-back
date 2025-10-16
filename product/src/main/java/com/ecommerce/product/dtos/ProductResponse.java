package com.ecommerce.product.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private String sellerId;
    private String sellerName;
    private String imageUrl; // Deprecated, kept for backward compatibility
    private Boolean active;

    // New fields
    private String brand;
    private Double rating;
    private Integer numReviews;
    private BigDecimal discountPrice;
    private List<String> imageUrls;
    private List<String> sizes;
    private List<String> colors;
    private String dressStyle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
