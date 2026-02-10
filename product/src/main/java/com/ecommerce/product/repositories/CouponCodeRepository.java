package com.ecommerce.product.repositories;

import com.ecommerce.product.models.CouponCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {
    
    Optional<CouponCode> findByCode(String code);
    
    Optional<CouponCode> findByCodeAndIsActiveTrue(String code);
    
    List<CouponCode> findBySellerId(String sellerId);
    
    List<CouponCode> findByCampaignId(Long campaignId);
    
    boolean existsByCode(String code);
}
