package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "inventory_alerts")
@Data
@NoArgsConstructor
public class InventoryAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String sellerId;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    private Integer threshold;

    private Boolean isActive = true;
    private Boolean isRead = false;

    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public enum AlertType {
        LOW_STOCK,      // Stock below threshold
        OUT_OF_STOCK,   // Stock = 0
        OVERSTOCK,      // Stock above max threshold
        BACK_IN_STOCK   // Previously out of stock, now available
    }
}
