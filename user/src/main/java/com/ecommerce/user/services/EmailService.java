package com.ecommerce.user.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending password reset emails.
 * This is a simplified implementation that logs emails for development.
 * In production, integrate with a real email service (SendGrid, AWS SES, etc.)
 */
@Service
@Slf4j
public class EmailService {
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.email.from:noreply@ecommerce.com}")
    private String fromEmail;
    
    /**
     * Send password reset email with reset link
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        // In development, log the email content
        log.info("========================================");
        log.info("PASSWORD RESET EMAIL");
        log.info("========================================");
        log.info("To: {}", toEmail);
        log.info("From: {}", fromEmail);
        log.info("Subject: Reset Your Password");
        log.info("----------------------------------------");
        log.info("Hello {},", userName);
        log.info("");
        log.info("You have requested to reset your password.");
        log.info("Click the link below to reset your password:");
        log.info("");
        log.info("Reset Link: {}", resetLink);
        log.info("");
        log.info("This link will expire in 1 hour.");
        log.info("");
        log.info("If you did not request this, please ignore this email.");
        log.info("========================================");
        
        // TODO: In production, send actual email using:
        // - Spring Mail with SMTP
        // - SendGrid
        // - AWS SES
        // - etc.
        
        /*
        Example with Spring Mail:
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset Your Password");
        message.setText(buildEmailContent(userName, resetLink));
        mailSender.send(message);
        */
    }
    
    /**
     * Send password reset confirmation email
     */
    public void sendPasswordResetConfirmation(String toEmail, String userName) {
        log.info("========================================");
        log.info("PASSWORD RESET CONFIRMATION EMAIL");
        log.info("========================================");
        log.info("To: {}", toEmail);
        log.info("From: {}", fromEmail);
        log.info("Subject: Your Password Has Been Reset");
        log.info("----------------------------------------");
        log.info("Hello {},", userName);
        log.info("");
        log.info("Your password has been successfully reset.");
        log.info("");
        log.info("If you did not make this change, please contact support immediately.");
        log.info("========================================");
    }
    
    private String buildEmailContent(String userName, String resetLink) {
        return String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Click the link below to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The E-commerce Team",
            userName, resetLink
        );
    }
    
    /**
     * Send account deletion confirmation email
     */
    public void sendAccountDeletionConfirmation(String toEmail, String userName) {
        log.info("========================================");
        log.info("ACCOUNT DELETION CONFIRMATION EMAIL");
        log.info("========================================");
        log.info("To: {}", toEmail);
        log.info("From: {}", fromEmail);
        log.info("Subject: Your Account Has Been Deleted");
        log.info("----------------------------------------");
        log.info("Hello {},", userName);
        log.info("");
        log.info("Your account has been successfully deleted.");
        log.info("All your personal data has been removed from our system.");
        log.info("");
        log.info("We're sorry to see you go. If you change your mind,");
        log.info("you can always create a new account at: {}", frontendUrl + "/register");
        log.info("");
        log.info("Thank you for being a part of our community.");
        log.info("========================================");
    }
}
