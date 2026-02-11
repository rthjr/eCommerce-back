package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.*;
import com.ecommerce.order.models.AnalyticsPeriod;
import com.ecommerce.order.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get analytics overview for a date range
     * GET /api/analytics/overview?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDTO> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getOverview(startDate, endDate));
    }

    /**
     * Get sales trend data based on period (DAILY, WEEKLY, MONTHLY)
     * GET /api/analytics/sales-trend?period=DAILY
     */
    @GetMapping("/sales-trend")
    public ResponseEntity<SalesTrendDTO> getSalesTrend(
            @RequestParam(defaultValue = "DAILY") AnalyticsPeriod period) {
        return ResponseEntity.ok(analyticsService.getSalesTrend(period));
    }

    /**
     * Get category performance analytics
     * GET /api/analytics/category-performance
     */
    @GetMapping("/category-performance")
    public ResponseEntity<CategoryPerformanceDTO> getCategoryPerformance() {
        return ResponseEntity.ok(analyticsService.getCategoryPerformance());
    }

    /**
     * Get payment method breakdown
     * GET /api/analytics/payment-methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<PaymentMethodBreakdownDTO> getPaymentMethodBreakdown() {
        return ResponseEntity.ok(analyticsService.getPaymentMethodBreakdown());
    }
}
