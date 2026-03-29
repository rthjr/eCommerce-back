package com.ecommerce.notification.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private Long orderId;
    private String userId;
    private String userRole; // ROLE_CUSTOMER, ROLE_ADMIN, etc.
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private LocalDateTime changedAt;
    private String message;
}
