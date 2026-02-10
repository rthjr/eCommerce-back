package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportResponse {
    private BigDecimal totalSales;
    private BigDecimal totalPlatformFees;
    private BigDecimal totalGatewayFees;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    private int ordersCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
