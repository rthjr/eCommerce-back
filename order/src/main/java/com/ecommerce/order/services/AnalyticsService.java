package com.ecommerce.order.services;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dtos.*;
import com.ecommerce.order.models.*;
import com.ecommerce.order.repositories.OrderRepository;
import com.ecommerce.order.repositories.PlatformMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PlatformMetricsRepository metricsRepository;
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    /**
     * Get analytics overview for a date range
     */
    public AnalyticsOverviewDTO getOverview(LocalDate startDate, LocalDate endDate) {
        // Validate dates
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Fetch metrics for the period
        List<PlatformMetrics> metrics = metricsRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);

        if (metrics.isEmpty()) {
            return createEmptyOverview(startDate, endDate);
        }

        // Calculate totals
        BigDecimal totalRevenue = metrics.stream()
                .map(PlatformMetrics::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGmv = metrics.stream()
                .map(PlatformMetrics::getGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = metrics.stream()
                .map(PlatformMetrics::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = metrics.stream()
                .map(PlatformMetrics::getFees)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalOrders = metrics.stream()
                .mapToLong(PlatformMetrics::getTotalOrders)
                .sum();

        Long totalNewUsers = metrics.stream()
                .mapToLong(PlatformMetrics::getNewUsers)
                .sum();

        Double averageActiveUsers = metrics.stream()
                .mapToLong(PlatformMetrics::getActiveUsers)
                .average()
                .orElse(0.0);

        Long totalPageViews = metrics.stream()
                .mapToLong(PlatformMetrics::getPageViews)
                .sum();

        // Calculate average order value
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalGmv.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate average page views per day
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Double averagePageViewsPerDay = daysDiff > 0
                ? totalPageViews.doubleValue() / daysDiff
                : 0.0;

        // Calculate growth rates (compare with previous period)
        LocalDate prevStartDate = startDate.minusDays(daysDiff);
        LocalDate prevEndDate = startDate.minusDays(1);

        BigDecimal revenueGrowthRate = calculateGrowthRate(
                metricsRepository.getTotalRevenueByDateRange(prevStartDate, prevEndDate),
                totalRevenue
        );

        Long prevTotalOrders = metricsRepository.getTotalOrdersByDateRange(prevStartDate, prevEndDate);
        BigDecimal orderGrowthRate = calculateGrowthRate(
                prevTotalOrders != null ? BigDecimal.valueOf(prevTotalOrders) : BigDecimal.ZERO,
                BigDecimal.valueOf(totalOrders)
        );

        AnalyticsOverviewDTO overview = new AnalyticsOverviewDTO();
        overview.setStartDate(startDate);
        overview.setEndDate(endDate);
        overview.setTotalRevenue(totalRevenue);
        overview.setTotalGmv(totalGmv);
        overview.setTotalCommission(totalCommission);
        overview.setTotalFees(totalFees);
        overview.setTotalOrders(totalOrders);
        overview.setAverageOrderValue(averageOrderValue);
        overview.setTotalNewUsers(totalNewUsers);
        overview.setAverageActiveUsers(averageActiveUsers);
        overview.setTotalPageViews(totalPageViews);
        overview.setAveragePageViewsPerDay(averagePageViewsPerDay);
        overview.setRevenueGrowthRate(revenueGrowthRate);
        overview.setOrderGrowthRate(orderGrowthRate);

        return overview;
    }

    /**
     * Get sales trend data based on period (DAILY, WEEKLY, MONTHLY)
     */
    public SalesTrendDTO getSalesTrend(AnalyticsPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        // Determine date range based on period
        switch (period) {
            case DAILY:
                startDate = endDate.minusDays(30); // Last 30 days
                break;
            case WEEKLY:
                startDate = endDate.minusWeeks(12); // Last 12 weeks
                break;
            case MONTHLY:
                startDate = endDate.minusMonths(12); // Last 12 months
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        List<PlatformMetrics> metrics = metricsRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);

        List<SalesTrendDTO.TrendDataPoint> trends = new ArrayList<>();

        if (period == AnalyticsPeriod.DAILY) {
            // Daily trends - one data point per day
            trends = metrics.stream()
                    .map(m -> new SalesTrendDTO.TrendDataPoint(
                            m.getDate(),
                            m.getDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                            m.getRevenue(),
                            m.getGmv(),
                            m.getTotalOrders(),
                            m.getActiveUsers()
                    ))
                    .collect(Collectors.toList());
        } else if (period == AnalyticsPeriod.WEEKLY) {
            // Weekly trends - group by week
            Map<Integer, List<PlatformMetrics>> weeklyGroups = metrics.stream()
                    .collect(Collectors.groupingBy(m -> m.getDate().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())));

            trends = weeklyGroups.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        List<PlatformMetrics> weekMetrics = entry.getValue();
                        LocalDate weekStart = weekMetrics.get(0).getDate();

                        return new SalesTrendDTO.TrendDataPoint(
                                weekStart,
                                "Week " + entry.getKey(),
                                sumRevenue(weekMetrics),
                                sumGmv(weekMetrics),
                                sumOrders(weekMetrics),
                                averageActiveUsers(weekMetrics)
                        );
                    })
                    .collect(Collectors.toList());
        } else if (period == AnalyticsPeriod.MONTHLY) {
            // Monthly trends - group by month
            Map<String, List<PlatformMetrics>> monthlyGroups = metrics.stream()
                    .collect(Collectors.groupingBy(m -> m.getDate().getYear() + "-" + String.format("%02d", m.getDate().getMonthValue())));

            trends = monthlyGroups.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        List<PlatformMetrics> monthMetrics = entry.getValue();
                        LocalDate monthStart = monthMetrics.get(0).getDate();

                        return new SalesTrendDTO.TrendDataPoint(
                                monthStart,
                                monthStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                                sumRevenue(monthMetrics),
                                sumGmv(monthMetrics),
                                sumOrders(monthMetrics),
                                averageActiveUsers(monthMetrics)
                        );
                    })
                    .collect(Collectors.toList());
        }

        SalesTrendDTO result = new SalesTrendDTO();
        result.setPeriod(period);
        result.setTrends(trends);
        return result;
    }

    /**
     * Get category performance analytics
     */
    public CategoryPerformanceDTO getCategoryPerformance() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);

        // Group order items by category
        Map<String, CategoryPerformanceDTO.CategoryData> categoryMap = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                try {
                    ProductResponse product = productServiceClient.getProductDetails(item.getProductId());
                    String category = product.getCategory() != null ? product.getCategory() : "Uncategorized";

                    CategoryPerformanceDTO.CategoryData categoryData = categoryMap.getOrDefault(category,
                            new CategoryPerformanceDTO.CategoryData(
                                    category,
                                    category,
                                    BigDecimal.ZERO,
                                    0L,
                                    0L,
                                    BigDecimal.ZERO
                            ));

                    BigDecimal itemRevenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    categoryData.setRevenue(categoryData.getRevenue().add(itemRevenue));
                    categoryData.setOrders(categoryData.getOrders() + 1);
                    categoryData.setUnits(categoryData.getUnits() + item.getQuantity());

                    categoryMap.put(category, categoryData);
                    totalRevenue = totalRevenue.add(itemRevenue);
                } catch (Exception e) {
                    // Skip if product not found
                    continue;
                }
            }
        }

        // Calculate percentages
        BigDecimal finalTotalRevenue = totalRevenue;
        List<CategoryPerformanceDTO.CategoryData> categories = categoryMap.values().stream()
                .peek(cat -> {
                    if (finalTotalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = cat.getRevenue()
                                .divide(finalTotalRevenue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                        cat.setPercentageOfTotal(percentage);
                    }
                })
                .sorted(Comparator.comparing(CategoryPerformanceDTO.CategoryData::getRevenue).reversed())
                .collect(Collectors.toList());

        CategoryPerformanceDTO result = new CategoryPerformanceDTO();
        result.setCategories(categories);
        return result;
    }

    /**
     * Get payment method breakdown
     */
    public PaymentMethodBreakdownDTO getPaymentMethodBreakdown() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);

        // Group by payment method
        Map<String, PaymentMethodBreakdownDTO.PaymentMethodData> paymentMap = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED || !order.getIsPaid()) {
                continue;
            }

            String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod() : "Unknown";
            BigDecimal orderAmount = order.getTotalAmount();

            PaymentMethodBreakdownDTO.PaymentMethodData data = paymentMap.getOrDefault(paymentMethod,
                    new PaymentMethodBreakdownDTO.PaymentMethodData(
                            paymentMethod,
                            BigDecimal.ZERO,
                            0L,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    ));

            data.setRevenue(data.getRevenue().add(orderAmount));
            data.setOrders(data.getOrders() + 1);

            paymentMap.put(paymentMethod, data);
            totalRevenue = totalRevenue.add(orderAmount);
        }

        // Calculate percentages and average order values
        BigDecimal finalTotalRevenue = totalRevenue;
        List<PaymentMethodBreakdownDTO.PaymentMethodData> paymentMethods = paymentMap.values().stream()
                .peek(pm -> {
                    if (finalTotalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = pm.getRevenue()
                                .divide(finalTotalRevenue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                        pm.setPercentageOfTotal(percentage);
                    }
                    if (pm.getOrders() > 0) {
                        BigDecimal avgOrderValue = pm.getRevenue()
                                .divide(BigDecimal.valueOf(pm.getOrders()), 2, RoundingMode.HALF_UP);
                        pm.setAverageOrderValue(avgOrderValue);
                    }
                })
                .sorted(Comparator.comparing(PaymentMethodBreakdownDTO.PaymentMethodData::getRevenue).reversed())
                .collect(Collectors.toList());

        PaymentMethodBreakdownDTO result = new PaymentMethodBreakdownDTO();
        result.setPaymentMethods(paymentMethods);
        result.setTotalRevenue(totalRevenue);
        return result;
    }

    // Helper methods

    private AnalyticsOverviewDTO createEmptyOverview(LocalDate startDate, LocalDate endDate) {
        AnalyticsOverviewDTO overview = new AnalyticsOverviewDTO();
        overview.setStartDate(startDate);
        overview.setEndDate(endDate);
        overview.setTotalRevenue(BigDecimal.ZERO);
        overview.setTotalGmv(BigDecimal.ZERO);
        overview.setTotalCommission(BigDecimal.ZERO);
        overview.setTotalFees(BigDecimal.ZERO);
        overview.setTotalOrders(0L);
        overview.setAverageOrderValue(BigDecimal.ZERO);
        overview.setTotalNewUsers(0L);
        overview.setAverageActiveUsers(0.0);
        overview.setTotalPageViews(0L);
        overview.setAveragePageViewsPerDay(0.0);
        overview.setRevenueGrowthRate(BigDecimal.ZERO);
        overview.setOrderGrowthRate(BigDecimal.ZERO);
        return overview;
    }

    private BigDecimal calculateGrowthRate(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumRevenue(List<PlatformMetrics> metrics) {
        return metrics.stream()
                .map(PlatformMetrics::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumGmv(List<PlatformMetrics> metrics) {
        return metrics.stream()
                .map(PlatformMetrics::getGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long sumOrders(List<PlatformMetrics> metrics) {
        return metrics.stream()
                .mapToLong(PlatformMetrics::getTotalOrders)
                .sum();
    }

    private Long averageActiveUsers(List<PlatformMetrics> metrics) {
        return (long) metrics.stream()
                .mapToLong(PlatformMetrics::getActiveUsers)
                .average()
                .orElse(0.0);
    }
}
