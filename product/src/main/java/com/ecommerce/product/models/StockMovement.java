package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "stock_movements")
@Data
@NoArgsConstructor
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String sellerId;

    private Integer quantity; // Positive for additions, negative for reductions

    private Integer previousStock;
    private Integer newStock;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private String reason;
    private String performedBy;

    private Long orderId; // If related to an order

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MovementType {
        ADD,           // Manual stock addition
        REMOVE,        // Manual stock removal
        ADJUST,        // Inventory adjustment/correction
        SALE,          // Sold to customer
        RETURN,        // Customer return
        DAMAGE,        // Damaged goods
        TRANSFER       // Stock transfer (future use)
    }
}
