package com.ecommerce.product.repositories;

import com.ecommerce.product.models.StockMovement;
import com.ecommerce.product.models.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    List<StockMovement> findByProductId(Long productId);
    
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
    
    List<StockMovement> findBySellerId(String sellerId);
    
    Page<StockMovement> findBySellerId(String sellerId, Pageable pageable);
    
    List<StockMovement> findByProductIdAndType(Long productId, MovementType type);
    
    @Query("SELECT sm FROM stock_movements sm WHERE sm.sellerId = :sellerId AND sm.createdAt BETWEEN :start AND :end")
    List<StockMovement> findBySellerIdAndDateRange(
            @Param("sellerId") String sellerId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(sm.quantity) FROM stock_movements sm WHERE sm.productId = :productId AND sm.type = :type")
    Long sumQuantityByProductIdAndType(@Param("productId") Long productId, @Param("type") MovementType type);
}
