package com.ecommerce.order.listeners;

import com.ecommerce.order.dtos.PaymentStatusMessage;
import com.ecommerce.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusListener {
    private final OrderService orderService;

    @RabbitListener(queues = "${payment.status.queue}")
    public void handlePaymentStatus(PaymentStatusMessage message) {
        if (message == null || message.getOrderId() == null || message.getOrderId().isBlank()) {
            log.warn("Skipping payment status message with missing orderId");
            return;
        }

        if (!"PAID".equalsIgnoreCase(message.getStatus())) {
            log.debug("Ignoring non-PAID payment status for orderId={}", message.getOrderId());
            return;
        }

        boolean updated = orderService.markAsPaidFromPaymentMessage(message.getOrderId(), message.getPaidAt());
        if (updated) {
            log.info("Order marked as PAID from payment status message: orderId={}", message.getOrderId());
        } else {
            log.warn("Failed to mark order as PAID from payment status message: orderId={}", message.getOrderId());
        }
    }
}
