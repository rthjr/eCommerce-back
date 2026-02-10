package com.ecommerce.product.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryOverviewResponse {
    private int totalProducts;
    private int totalStock;
    private int outOfStock;
    private int lowStock;
    private int healthyStock;
    private long unreadAlerts;
}
