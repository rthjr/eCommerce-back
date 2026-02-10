package com.ecommerce.product.repositories;

import com.ecommerce.product.models.Campaign;
import com.ecommerce.product.models.Campaign.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    List<Campaign> findBySellerId(String sellerId);
    
    Page<Campaign> findBySellerId(String sellerId, Pageable pageable);
    
    List<Campaign> findBySellerIdAndStatus(String sellerId, CampaignStatus status);
    
    List<Campaign> findBySellerIdAndIsActiveTrue(String sellerId);
    
    @Query("SELECT c FROM campaigns c WHERE c.sellerId = :sellerId AND c.startDate <= :now AND c.endDate >= :now AND c.isActive = true")
    List<Campaign> findActiveRunningCampaigns(@Param("sellerId") String sellerId, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM campaigns c WHERE c.status = 'SCHEDULED' AND c.startDate <= :now")
    List<Campaign> findCampaignsToActivate(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM campaigns c WHERE c.status = 'ACTIVE' AND c.endDate < :now")
    List<Campaign> findCampaignsToEnd(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(c) FROM campaigns c WHERE c.sellerId = :sellerId AND c.status = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") String sellerId, @Param("status") CampaignStatus status);
    
    boolean existsBySellerIdAndNameAndIdNot(String sellerId, String name, Long id);
}
