package com.ecommerce.order.repositories;

import com.ecommerce.order.models.ReturnRequest;
import com.ecommerce.order.models.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByUserId(String userId);
    List<ReturnRequest> findByOrderId(Long orderId);
    Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable);
    Page<ReturnRequest> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Seller-scoped queries
    Page<ReturnRequest> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);
    Page<ReturnRequest> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, ReturnStatus status, Pageable pageable);
    Long countBySellerId(String sellerId);
    Long countBySellerIdAndStatus(String sellerId, ReturnStatus status);
}
