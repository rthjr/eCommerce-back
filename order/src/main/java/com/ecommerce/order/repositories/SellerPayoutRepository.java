package com.ecommerce.order.repositories;

import com.ecommerce.order.models.SellerPayout;
import com.ecommerce.order.models.SellerPayout.PayoutStatus;
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
public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {
    
    List<SellerPayout> findBySellerId(String sellerId);
    
    Page<SellerPayout> findBySellerId(String sellerId, Pageable pageable);
    
    List<SellerPayout> findBySellerIdAndStatus(String sellerId, PayoutStatus status);
    
    @Query("SELECT SUM(p.netAmount) FROM seller_payouts p WHERE p.sellerId = :sellerId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidToSeller(@Param("sellerId") String sellerId);
    
    @Query("SELECT SUM(p.netAmount) FROM seller_payouts p WHERE p.sellerId = :sellerId AND p.status = 'PENDING'")
    BigDecimal getPendingPayoutAmount(@Param("sellerId") String sellerId);
    
    @Query("SELECT p FROM seller_payouts p WHERE p.status = :status")
    List<SellerPayout> findByStatus(@Param("status") PayoutStatus status);
    
    @Query("SELECT p FROM seller_payouts p WHERE p.sellerId = :sellerId AND p.createdAt BETWEEN :start AND :end")
    List<SellerPayout> findBySellerIdAndDateRange(
            @Param("sellerId") String sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
