package com.ecommerce.notification.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String orderId;
    private String userId;
    private String userRole; // ROLE_CUSTOMER, ROLE_ADMIN, etc.
    private PaymentEventType eventType;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime timestamp;
    private String message;
    private String failureReason; // Only for failed payments
}
