package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class SellerFAQResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String question;
    private String answer;
    private Boolean hidden;
}
