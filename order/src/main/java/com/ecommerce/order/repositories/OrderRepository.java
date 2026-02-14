package com.ecommerce.order.repositories;

import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    
    // Seller order queries - orders containing products from a specific seller
    @Query("SELECT DISTINCT o FROM orders o JOIN FETCH o.items i")
    List<Order> findAllOrdersWithItems();
    
    List<Order> findByStatus(OrderStatus status);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM orders o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM orders o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM orders o WHERE o.createdAt BETWEEN :start AND :end AND o.status != 'CANCELLED'")
    BigDecimal getTotalRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM orders o WHERE o.createdAt BETWEEN :start AND :end")
    Long getOrderCountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Orders by payment method
    List<Order> findByPaymentMethod(String paymentMethod);
    
    // Orders that need attention (pending, not paid for COD)
    @Query("SELECT o FROM orders o WHERE o.status = 'PENDING' OR (o.paymentMethod = 'CASH_ON_DELIVERY' AND o.isPaid = false)")
    List<Order> findOrdersNeedingAttention();
}
