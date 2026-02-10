package com.ecommerce.product.dtos;

import com.ecommerce.product.models.InventoryAlert.AlertType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryAlertResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer currentStock;
    private AlertType type;
    private Integer threshold;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
