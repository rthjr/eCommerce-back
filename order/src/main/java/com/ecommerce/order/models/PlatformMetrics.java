package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "platform_metrics")
@Data
@NoArgsConstructor
public class PlatformMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate date;

    @Column(precision = 19, scale = 2)
    private BigDecimal gmv = BigDecimal.ZERO; // Gross Merchandise Value

    @Column(precision = 19, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    private Long totalOrders = 0L;

    private Long activeUsers = 0L;

    private Long newUsers = 0L;

    private Long pageViews = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
