package com.ecom.payment.controller;

import com.ecom.payment.client.PaymentGatewayClient;
import com.ecom.payment.dto.CreateOrderRequest;
import com.ecom.payment.dto.OrderResponse;
import com.ecom.payment.dto.OrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayClient gatewayClient;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(gatewayClient.createOrder(request));
    }

    @PostMapping("/orders/{orderId}/test-paid")
    public ResponseEntity<OrderStatusResponse> markOrderPaid(@PathVariable String orderId) {
        return ResponseEntity.ok(gatewayClient.markOrderPaid(orderId));
    }

    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(gatewayClient.getOrderStatus(orderId));
    }
}
