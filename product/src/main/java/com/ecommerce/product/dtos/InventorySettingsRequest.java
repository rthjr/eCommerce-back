package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class InventorySettingsRequest {
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String sku;
}
