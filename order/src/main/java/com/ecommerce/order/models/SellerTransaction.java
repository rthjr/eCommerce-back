package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "seller_transactions")
@Data
@NoArgsConstructor
public class SellerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal paymentGatewayFee;
    private BigDecimal netAmount;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "payout_id")
    private SellerPayout payout;

    private Boolean isSettled = false;
    private LocalDateTime settledAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum TransactionType {
        SALE,           // Order completed
        REFUND,         // Refund issued
        PLATFORM_FEE,   // Platform commission
        GATEWAY_FEE,    // Payment gateway fee
        PAYOUT,         // Money paid to seller
        ADJUSTMENT      // Manual adjustment
    }

    public enum TransactionStatus {
        PENDING,        // Waiting for order completion
        HOLD,           // In hold period (14 days)
        AVAILABLE,      // Available for payout
        SETTLED,        // Included in a payout
        CANCELLED       // Refunded or cancelled
    }
}
