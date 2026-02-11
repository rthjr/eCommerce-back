package com.ecommerce.order.repositories;

import com.ecommerce.order.models.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {
    List<DeliveryAttempt> findByOrderIdOrderByAttemptNumberDesc(Long orderId);

    @Query("SELECT MAX(d.attemptNumber) FROM delivery_attempts d WHERE d.orderId = :orderId")
    Optional<Integer> findMaxAttemptNumberByOrderId(@Param("orderId") Long orderId);

    List<DeliveryAttempt> findByRiderId(String riderId);

    Long countByOrderId(Long orderId);
}
