package com.ecommerce.product.repositories;

import com.ecommerce.product.models.InventoryAlert;
import com.ecommerce.product.models.InventoryAlert.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {
    
    List<InventoryAlert> findBySellerId(String sellerId);
    
    Page<InventoryAlert> findBySellerId(String sellerId, Pageable pageable);
    
    List<InventoryAlert> findBySellerIdAndIsActiveTrue(String sellerId);
    
    List<InventoryAlert> findBySellerIdAndIsReadFalse(String sellerId);
    
    List<InventoryAlert> findByProductId(Long productId);
    
    List<InventoryAlert> findBySellerIdAndType(String sellerId, AlertType type);
    
    Long countBySellerIdAndIsReadFalse(String sellerId);
    
    void deleteByProductId(Long productId);
}
