package com.ecommerce.order.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    // Shipping Information
    @Embedded
    private ShippingAddress shippingAddress;

    // Payment Information
    private String paymentMethod;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "paymentId", column = @Column(name = "payment_id")),
            @AttributeOverride(name = "status", column = @Column(name = "payment_status")),
            @AttributeOverride(name = "updateTime", column = @Column(name = "payment_update_time")),
            @AttributeOverride(name = "emailAddress", column = @Column(name = "payment_email_address"))
    })
    private PaymentResult paymentResult;

    // Price Breakdown
    private BigDecimal itemsPrice;
    private BigDecimal taxPrice;
    private BigDecimal shippingPrice;

    // Payment Status
    private Boolean isPaid = false;
    private LocalDateTime paidAt;

    // Delivery Status
    private Boolean isDelivered = false;
    private LocalDateTime deliveredAt;

    // Failed Delivery Information
    private Integer failedDeliveryAttempts = 0;
    private String failedDeliveryReason;

    // Payment Gateway IDs
    private String paypalOrderId;
    private String stripeClientSecret;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
