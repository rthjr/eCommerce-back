package com.ecommerce.order.dtos;

import com.ecommerce.order.models.RefundMethod;
import com.ecommerce.order.models.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundDTO {
    private Long id;
    private Long orderId;
    private Long returnRequestId;
    private BigDecimal amount;
    private RefundMethod method;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
