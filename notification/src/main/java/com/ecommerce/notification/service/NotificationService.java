package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.ecommerce.notification.client.UserServiceClient;
import com.ecommerce.notification.dto.UserDTO;
import com.ecommerce.notification.payload.PaymentEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final UserServiceClient userServiceClient;
	private final EmailService emailService;

	public void sendPaymentSuccessNotification(PaymentEvent event) {
		try {
			UserDTO user = userServiceClient.getUserById(event.getUserId());

			if (user != null && user.getEmail() != null) {
				emailService.sendPaymentSuccessEmail(user.getEmail(), user.getName(), event);
				log.info("📧 Payment success notification sent to user: {}", user.getEmail());
			} else {
				log.warn("⚠️ User email not found for userId: {}", event.getUserId());
			}

		} catch (Exception e) {
			log.error("❌ Failed to send payment success notification for user {}: {}", event.getUserId(),
					e.getMessage());
		}
	}

	public void sendPaymentFailedNotification(PaymentEvent event) {
		try {
			UserDTO user = userServiceClient.getUserById(event.getUserId());

			if (user != null && user.getEmail() != null) {
				emailService.sendPaymentFailedEmail(user.getEmail(), user.getName(), event);
				log.info("📧 Payment failed notification sent to user: {}", user.getEmail());
			} else {
				log.warn("⚠️ User email not found for userId: {}", event.getUserId());
			}

		} catch (Exception e) {
			log.error("❌ Failed to send payment failed notification for user {}: {}", event.getUserId(),
					e.getMessage());
		}
	}
}
