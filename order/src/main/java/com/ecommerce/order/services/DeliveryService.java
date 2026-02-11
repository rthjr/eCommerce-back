package com.ecommerce.order.services;

import com.ecommerce.order.dtos.DeliveryAttemptDTO;
import com.ecommerce.order.dtos.RecordFailedDeliveryDTO;
import com.ecommerce.order.dtos.RescheduleDeliveryDTO;
import com.ecommerce.order.models.DeliveryAttempt;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.DeliveryAttemptRepository;
import com.ecommerce.order.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final OrderRepository orderRepository;

    /**
     * Record a failed delivery attempt for an order
     */
    @Transactional
    public Optional<DeliveryAttemptDTO> recordFailedDelivery(Long orderId, RecordFailedDeliveryDTO request) {
        // Find the order
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Get the current attempt number (increment from the last attempt)
        Integer lastAttemptNumber = deliveryAttemptRepository.findMaxAttemptNumberByOrderId(orderId)
                .orElse(0);
        Integer newAttemptNumber = lastAttemptNumber + 1;

        // Create a new delivery attempt
        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setOrderId(orderId);
        attempt.setRiderId(request.getRiderId());
        attempt.setAttemptNumber(newAttemptNumber);
        attempt.setFailureReason(request.getFailureReason());
        attempt.setNotes(request.getNotes());
        attempt.setAttemptDate(LocalDateTime.now());

        DeliveryAttempt savedAttempt = deliveryAttemptRepository.save(attempt);

        // Update order status to reflect failed delivery
        if (newAttemptNumber >= 3) {
            // After 3 failed attempts, mark order as failed
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            // Otherwise, mark as pending delivery
            order.setStatus(OrderStatus.PENDING);
        }
        order.setIsDelivered(false);
        orderRepository.save(order);

        return Optional.of(mapToDTO(savedAttempt));
    }

    /**
     * Get all delivery attempts for an order
     */
    public List<DeliveryAttemptDTO> getDeliveryAttempts(Long orderId) {
        List<DeliveryAttempt> attempts = deliveryAttemptRepository.findByOrderIdOrderByAttemptNumberDesc(orderId);
        return attempts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reschedule delivery for an order
     */
    @Transactional
    public Optional<String> rescheduleDelivery(Long orderId, RescheduleDeliveryDTO request) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Check if the order is eligible for rescheduling
        if (order.getIsDelivered()) {
            return Optional.of("Order has already been delivered");
        }

        // Count failed delivery attempts
        Long attemptCount = deliveryAttemptRepository.countByOrderId(orderId);
        if (attemptCount >= 3) {
            return Optional.of("Order has exceeded maximum delivery attempts");
        }

        // Update order status to allow for rescheduled delivery
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // In a real-world scenario, you might want to store the preferred date/time
        // For now, we'll return a success message
        String message = String.format("Delivery rescheduled for %s at %s",
                request.getPreferredDate(),
                request.getPreferredTime());

        return Optional.of(message);
    }

    /**
     * Map DeliveryAttempt entity to DTO
     */
    private DeliveryAttemptDTO mapToDTO(DeliveryAttempt attempt) {
        return new DeliveryAttemptDTO(
                attempt.getId(),
                attempt.getOrderId(),
                attempt.getRiderId(),
                attempt.getAttemptNumber(),
                attempt.getFailureReason(),
                attempt.getNotes(),
                attempt.getAttemptDate(),
                attempt.getCreatedAt()
        );
    }
}
