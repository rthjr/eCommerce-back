package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class LowStockProductResponse {
    private Long id;
    private String name;
    private String sku;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String imageUrl;
}
