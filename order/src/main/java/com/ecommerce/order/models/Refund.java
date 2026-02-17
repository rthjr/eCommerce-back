package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "refunds")
@Data
@NoArgsConstructor
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "return_request_id")
    private Long returnRequestId;

    @Column(name = "seller_id")
    private String sellerId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RefundMethod method = RefundMethod.ORIGINAL;

    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;
}
