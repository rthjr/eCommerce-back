package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAttemptDTO {
    private Long id;
    private Long orderId;
    private String riderId;
    private Integer attemptNumber;
    private String failureReason;
    private String notes;
    private LocalDateTime attemptDate;
    private LocalDateTime createdAt;
}
