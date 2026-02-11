package com.ecommerce.order.dtos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsOverviewDTO {
    private LocalDate startDate;
    private LocalDate endDate;

    // Financial Metrics
    private BigDecimal totalRevenue;
    private BigDecimal totalGmv;
    private BigDecimal totalCommission;
    private BigDecimal totalFees;

    // Order Metrics
    private Long totalOrders;
    private BigDecimal averageOrderValue;

    // User Metrics
    private Long totalNewUsers;
    private Double averageActiveUsers;

    // Engagement Metrics
    private Long totalPageViews;
    private Double averagePageViewsPerDay;

    // Growth Metrics
    private BigDecimal revenueGrowthRate;
    private BigDecimal orderGrowthRate;
}
