package com.ecommerce.order.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemRequest {
    private String productId;
    private Integer quantity;

    // Product details for display
    private String productName;
    private String productImage;
    private BigDecimal price;
    private String selectedColor;
    private String selectedSize;
}
