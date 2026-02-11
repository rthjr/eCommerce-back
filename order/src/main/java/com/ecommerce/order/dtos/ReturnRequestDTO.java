package com.ecommerce.order.dtos;

import com.ecommerce.order.models.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestDTO {
    private Long id;
    private Long orderId;
    private String productId;
    private String userId;
    private String reason;
    private List<String> photos;
    private ReturnStatus status;
    private BigDecimal refundAmount;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
}
