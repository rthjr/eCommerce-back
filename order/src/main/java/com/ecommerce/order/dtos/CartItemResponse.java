package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private String productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private String selectedColor;
    private String selectedSize;
}
