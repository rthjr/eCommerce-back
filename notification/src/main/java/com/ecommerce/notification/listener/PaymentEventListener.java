package com.ecommerce.notification.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ecommerce.notification.payload.PaymentEvent;
import com.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = "payment.events.queue")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("📨 Received payment event from queue: payment.events.queue");
        log.info("Payment event for order: {} - Type: {}", event.getOrderId(), event.getEventType());
        
        if ("ROLE_CUSTOMER".equals(event.getUserRole())) {
            switch (event.getEventType()) {
                case PAYMENT_SUCCESS:
                    log.info("💳 PAYMENT SUCCESS: Order {} - Amount: {} {}", 
                            event.getOrderId(), event.getAmount(), event.getCurrency());
                    notificationService.sendPaymentSuccessNotification(event);
                    break;
                case PAYMENT_FAILED:
                    log.info("❌ PAYMENT FAILED: Order {} - Reason: {}", 
                            event.getOrderId(), event.getFailureReason());
                    notificationService.sendPaymentFailedNotification(event);
                    break;
                case REFUND_PROCESSED:
                    log.info("💰 REFUND PROCESSED: Order {} - Amount: {} {}", 
                            event.getOrderId(), event.getAmount(), event.getCurrency());
                    break;
            }
        }
    }
}
