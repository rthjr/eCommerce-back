package com.ecom.payment.controller;

import com.ecom.payment.client.PaymentGatewayClient;
import com.ecom.payment.dto.CreateOrderRequest;
import com.ecom.payment.dto.OrderResponse;
import com.ecom.payment.dto.OrderStatusResponse;
import com.ecom.payment.event.PaymentStatusMessage;
import com.ecom.payment.messaging.PaymentStatusPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentGatewayClient gatewayClient;
    private final PaymentStatusPublisher paymentStatusPublisher;

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
        OrderStatusResponse response = gatewayClient.getOrderStatus(orderId);
        if (response != null && "PAID".equalsIgnoreCase(response.getStatus())) {
            String resolvedOrderId = orderId != null ? orderId : response.getOrder_id();
            paymentStatusPublisher.publishPaid(
                    new PaymentStatusMessage(resolvedOrderId, "PAID", OffsetDateTime.now().toString())
            );
            log.info("Published payment PAID event for orderId={}", resolvedOrderId);
        }
        return ResponseEntity.ok(response);
    }
}
