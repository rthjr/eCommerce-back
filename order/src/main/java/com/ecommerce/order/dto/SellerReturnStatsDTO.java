package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for seller return statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerReturnStatsDTO {
    private Long totalReturns;
    private Long pendingReturns;
    private Long approvedReturns;
    private Long rejectedReturns;
    private Long completedReturns;
    private Double totalRefundAmount;
    private Double averageRefundAmount;
}
