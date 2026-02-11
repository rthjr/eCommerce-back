package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for approving a return request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveReturnRequestDTO {
    private BigDecimal refundAmount;
}
