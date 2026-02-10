package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class StockAdjustmentRequest {
    private Integer quantity;
    private String reason;
    private String performedBy;
}
