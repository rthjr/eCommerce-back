package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "delivery_attempts")
@Data
@NoArgsConstructor
public class DeliveryAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "rider_id")
    private String riderId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
