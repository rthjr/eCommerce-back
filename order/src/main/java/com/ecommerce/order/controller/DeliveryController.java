package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.DeliveryAttemptDTO;
import com.ecommerce.order.dtos.RecordFailedDeliveryDTO;
import com.ecommerce.order.dtos.RescheduleDeliveryDTO;
import com.ecommerce.order.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class DeliveryController {
    private final DeliveryService deliveryService;

    /**
     * Record a failed delivery attempt
     * POST /api/orders/{id}/delivery-failed
     */
    @PostMapping("/{id}/delivery-failed")
    public ResponseEntity<DeliveryAttemptDTO> recordFailedDelivery(
            @PathVariable Long id,
            @RequestBody RecordFailedDeliveryDTO request) {
        return deliveryService.recordFailedDelivery(id, request)
                .map(attempt -> new ResponseEntity<>(attempt, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all delivery attempts for an order
     * GET /api/orders/{id}/delivery-attempts
     */
    @GetMapping("/{id}/delivery-attempts")
    public ResponseEntity<List<DeliveryAttemptDTO>> getDeliveryAttempts(@PathVariable Long id) {
        List<DeliveryAttemptDTO> attempts = deliveryService.getDeliveryAttempts(id);
        return ResponseEntity.ok(attempts);
    }

    /**
     * Reschedule delivery for an order
     * POST /api/orders/{id}/reschedule
     */
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<String> rescheduleDelivery(
            @PathVariable Long id,
            @RequestBody RescheduleDeliveryDTO request) {
        return deliveryService.rescheduleDelivery(id, request)
                .map(message -> ResponseEntity.ok(message))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
