package com.ecommerce.product.dtos;

import com.ecommerce.product.models.StockMovement.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private MovementType type;
    private String reason;
    private String performedBy;
    private Long orderId;
    private LocalDateTime createdAt;
}
