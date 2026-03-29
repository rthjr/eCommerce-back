package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ecommerce.notification.payload.PaymentEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Async
    public void sendPaymentSuccessEmail(String toEmail, String userName, PaymentEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("✅ Payment Successful - Order " + event.getOrderId());
            message.setText(buildPaymentSuccessContent(userName, event));
            
            mailSender.send(message);
            log.info("✓ Payment success email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("❌ Failed to send payment success email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    @Async
    public void sendPaymentFailedEmail(String toEmail, String userName, PaymentEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("❌ Payment Failed - Order " + event.getOrderId());
            message.setText(buildPaymentFailedContent(userName, event));
            
            mailSender.send(message);
            log.info("✓ Payment failed email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("❌ Failed to send payment failed email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    private String buildPaymentSuccessContent(String userName, PaymentEvent event) {
        return String.format(
            "Hello %s,\n\n" +
            "✅ Your payment has been successfully processed!\n\n" +
            "Payment Details:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Order ID: %s\n" +
            "Amount Paid: %s %s\n" +
            "Payment Method: %s\n" +
            "Transaction ID: %s\n" +
            "Date: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Your order is now confirmed and will be processed shortly.\n\n" +
            "Thank you for your purchase!\n\n" +
            "Best regards,\n" +
            "E-Commerce Team",
            userName,
            event.getOrderId(),
            event.getAmount(),
            event.getCurrency(),
            event.getPaymentMethod(),
            event.getTransactionId(),
            event.getTimestamp()
        );
    }
    
    private String buildPaymentFailedContent(String userName, PaymentEvent event) {
        return String.format(
            "Hello %s,\n\n" +
            "❌ Your payment could not be processed.\n\n" +
            "Payment Details:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Order ID: %s\n" +
            "Amount: %s %s\n" +
            "Payment Method: %s\n" +
            "Reason: %s\n" +
            "Date: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Please try again to complete your order.\n\n" +
            "If you continue to experience issues, please contact our support team.\n\n" +
            "Best regards,\n" +
            "E-Commerce Team",
            userName,
            event.getOrderId(),
            event.getAmount(),
            event.getCurrency(),
            event.getPaymentMethod(),
            event.getFailureReason() != null ? event.getFailureReason() : "Payment verification failed",
            event.getTimestamp()
        );
    }
}
