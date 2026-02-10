package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "seller_payouts")
@Data
@NoArgsConstructor
public class SellerPayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerId;

    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal paymentGatewayFee;
    private BigDecimal netAmount;

    private LocalDateTime periodStartDate;
    private LocalDateTime periodEndDate;

    private Integer ordersCount;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status = PayoutStatus.PENDING;

    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;

    private String transactionReference;
    private String notes;

    private LocalDateTime processedAt;
    private String processedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PayoutStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
