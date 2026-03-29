package com.ecommerce.notification;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.ecommerce.notification.payload.OrderCreatedEvent;
import com.ecommerce.notification.payload.OrderStatus;
import com.ecommerce.notification.payload.OrderStatusChangedEvent;
import com.ecommerce.notification.payload.PaymentEvent;
import com.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

	private final NotificationService notificationService;

	@Bean
	public Consumer<OrderCreatedEvent> orderCreated() {
		return event -> {
			log.info("Received order created event for order: {}", event.getOrderId());
			log.info("Received order created event for user id: {}", event.getUserId());
		};
	}

	@Bean
	public Consumer<OrderStatusChangedEvent> orderStatusChanged() {
		return event -> {
			log.info("Order status changed for order: {} from {} to {}", event.getOrderId(), event.getPreviousStatus(),
					event.getNewStatus());

			if ("ROLE_CUSTOMER".equals(event.getUserRole()) && event.getNewStatus() == OrderStatus.DELIVERED) {
				log.info("🎉 CUSTOMER NOTIFICATION: Order {} has been delivered!", event.getOrderId());
				sendDeliveryNotification(event);
			}

			if ("ROLE_CUSTOMER".equals(event.getUserRole())) {
				switch (event.getNewStatus()) {
				case CONFIRMED:
					log.info("✅ CUSTOMER NOTIFICATION: Order {} confirmed", event.getOrderId());
					break;
				case SHIPPED:
					log.info("🚚 CUSTOMER NOTIFICATION: Order {} shipped", event.getOrderId());
					break;
				case CANCELLED:
					log.info("❌ CUSTOMER NOTIFICATION: Order {} cancelled", event.getOrderId());
					break;
				default:
					break;
				}
			}
		};
	}

	@Bean
	public Consumer<PaymentEvent> paymentEvent() {
		return event -> {
			log.info("Payment event for order: {} - Type: {}", event.getOrderId(), event.getEventType());

			if ("ROLE_CUSTOMER".equals(event.getUserRole())) {
				switch (event.getEventType()) {
				case PAYMENT_SUCCESS:
					log.info("💳 PAYMENT SUCCESS: Order {} - Amount: {} {}", event.getOrderId(), event.getAmount(),
							event.getCurrency());
					notificationService.sendPaymentSuccessNotification(event);
					break;
				case PAYMENT_FAILED:
					log.info("❌ PAYMENT FAILED: Order {} - Reason: {}", event.getOrderId(), event.getFailureReason());
					notificationService.sendPaymentFailedNotification(event);
					break;
				case REFUND_PROCESSED:
					log.info("💰 REFUND PROCESSED: Order {} - Amount: {} {}", event.getOrderId(), event.getAmount(),
							event.getCurrency());
					sendRefundProcessedNotification(event);
					break;
				}
			}
		};
	}

	private void sendDeliveryNotification(OrderStatusChangedEvent event) {
		log.info("📦 Sending delivery notification to user {} for order {}", event.getUserId(), event.getOrderId());
	}

	private void sendRefundProcessedNotification(PaymentEvent event) {
		log.info("💵 Sending refund processed notification to user {} for order {}", event.getUserId(),
				event.getOrderId());
	}
}
