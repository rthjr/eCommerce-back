package com.ecommerce.order.dtos;

import com.ecommerce.order.models.SellerTransaction.TransactionStatus;
import com.ecommerce.order.models.SellerTransaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellerTransactionResponse {
    private Long id;
    private String sellerId;
    private Long orderId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal paymentGatewayFee;
    private BigDecimal netAmount;
    private String description;
    private TransactionStatus status;
    private Long payoutId;
    private Boolean isSettled;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
}
