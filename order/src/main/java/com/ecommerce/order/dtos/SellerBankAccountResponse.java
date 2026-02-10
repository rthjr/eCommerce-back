package com.ecommerce.order.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerBankAccountResponse {
    private Long id;
    private String sellerId;
    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    private Boolean isPrimary;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
