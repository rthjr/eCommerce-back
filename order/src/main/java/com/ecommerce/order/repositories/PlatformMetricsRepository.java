package com.ecommerce.order.repositories;

import com.ecommerce.order.models.PlatformMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformMetricsRepository extends JpaRepository<PlatformMetrics, Long> {

    Optional<PlatformMetrics> findByDate(LocalDate date);

    List<PlatformMetrics> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT pm FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end ORDER BY pm.date ASC")
    List<PlatformMetrics> findMetricsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(pm.revenue) FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end")
    BigDecimal getTotalRevenueByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(pm.gmv) FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end")
    BigDecimal getTotalGmvByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(pm.totalOrders) FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end")
    Long getTotalOrdersByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT AVG(pm.activeUsers) FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end")
    Double getAverageActiveUsersByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(pm.newUsers) FROM platform_metrics pm WHERE pm.date BETWEEN :start AND :end")
    Long getTotalNewUsersByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
