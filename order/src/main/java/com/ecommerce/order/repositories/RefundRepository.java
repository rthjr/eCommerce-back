package com.ecommerce.order.repositories;

import com.ecommerce.order.models.Refund;
import com.ecommerce.order.models.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByOrderId(Long orderId);
    Optional<Refund> findByReturnRequestId(Long returnRequestId);
    List<Refund> findByStatus(RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.amount), 0.0) FROM refunds r WHERE r.sellerId = :sellerId AND r.status = 'COMPLETED'")
    Double sumAmountBySellerId(@Param("sellerId") String sellerId);
}
