package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "return_requests")
@Data
@NoArgsConstructor
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "seller_id")
    private String sellerId;

    private String reason;

    @ElementCollection
    @CollectionTable(name = "return_photos", joinColumns = @JoinColumn(name = "return_request_id"))
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private List<String> photos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
