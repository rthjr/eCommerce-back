package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.CreateOrderRequest;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.PaymentResultDTO;
import com.ecommerce.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody(required = false) CreateOrderRequest request) {
        return orderService.createOrder(userId, request)
                .map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.CREATED))
                .orElseGet(() -> {
                    log.error("[createOrder] Failed to create order for userId={} - service returned empty result", userId);
                    return ResponseEntity.badRequest().build();
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("[getOrderById] Order not found with id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/myorders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(
            @PathVariable Long id,
            @RequestBody PaymentResultDTO paymentResult) {
        return orderService.markAsPaid(id, paymentResult)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("[markAsPaid] Order not found with id={} - cannot mark as paid", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> markAsDelivered(@PathVariable Long id) {
        return orderService.markAsDelivered(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("[markAsDelivered] Order not found with id={} - cannot mark as delivered", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
