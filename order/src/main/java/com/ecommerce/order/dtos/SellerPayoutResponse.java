package com.ecommerce.order.dtos;

import com.ecommerce.order.models.SellerPayout.PayoutStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellerPayoutResponse {
    private Long id;
    private String sellerId;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal paymentGatewayFee;
    private BigDecimal netAmount;
    private LocalDateTime periodStartDate;
    private LocalDateTime periodEndDate;
    private Integer ordersCount;
    private PayoutStatus status;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String transactionReference;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
