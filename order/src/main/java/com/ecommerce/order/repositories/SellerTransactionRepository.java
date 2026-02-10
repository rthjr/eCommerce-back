package com.ecommerce.order.repositories;

import com.ecommerce.order.models.SellerTransaction;
import com.ecommerce.order.models.SellerTransaction.TransactionStatus;
import com.ecommerce.order.models.SellerTransaction.TransactionType;
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
public interface SellerTransactionRepository extends JpaRepository<SellerTransaction, Long> {
    
    List<SellerTransaction> findBySellerId(String sellerId);
    
    Page<SellerTransaction> findBySellerId(String sellerId, Pageable pageable);
    
    List<SellerTransaction> findBySellerIdAndType(String sellerId, TransactionType type);
    
    List<SellerTransaction> findBySellerIdAndStatus(String sellerId, TransactionStatus status);
    
    List<SellerTransaction> findByOrderId(Long orderId);
    
    List<SellerTransaction> findByPayoutId(Long payoutId);
    
    @Query("SELECT SUM(t.netAmount) FROM seller_transactions t WHERE t.sellerId = :sellerId AND t.status = 'AVAILABLE' AND t.isSettled = false")
    BigDecimal getAvailableBalance(@Param("sellerId") String sellerId);
    
    @Query("SELECT SUM(t.netAmount) FROM seller_transactions t WHERE t.sellerId = :sellerId AND t.status = 'HOLD'")
    BigDecimal getPendingBalance(@Param("sellerId") String sellerId);
    
    @Query("SELECT SUM(t.amount) FROM seller_transactions t WHERE t.sellerId = :sellerId AND t.type = 'SALE' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalSalesByDateRange(
            @Param("sellerId") String sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM seller_transactions t WHERE t.sellerId = :sellerId AND t.status = 'AVAILABLE' AND t.isSettled = false")
    List<SellerTransaction> findAvailableForPayout(@Param("sellerId") String sellerId);
    
    @Query("SELECT t FROM seller_transactions t WHERE t.status = 'HOLD' AND t.createdAt < :holdEndDate")
    List<SellerTransaction> findTransactionsReadyForRelease(@Param("holdEndDate") LocalDateTime holdEndDate);
}
