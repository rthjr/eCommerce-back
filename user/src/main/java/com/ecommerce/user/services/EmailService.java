package com.ecommerce.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ecommerce.user.exception.EmailSendingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    /**
     * Send password reset email with 6-digit code
     * NOTE: This method is called from a transactional context and MUST throw exceptions
     * so the transaction can be rolled back if email fails
     */
    public void sendPasswordResetCode(String toEmail, String userName, String resetCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Password Reset Code");
            message.setText(buildResetCodeEmailContent(userName, resetCode));

            log.debug("Attempting to send password reset code email to: {}", toEmail);
            mailSender.send(message);
            log.info("✓ Password reset code email sent successfully to: {}", toEmail);

        } catch (MailException e) {
            log.error("❌ Failed to send password reset code email to {}", toEmail);
            log.error("SMTP Error details: {}", e.getMessage(), e);
            throw new EmailSendingException(
                "Failed to send password reset email",
                e,
                toEmail,
                "PASSWORD_RESET_CODE"
            );
        } catch (Exception e) {
            log.error("❌ Unexpected error sending password reset code email to {}", toEmail, e);
            throw new EmailSendingException(
                "Unexpected error sending password reset email",
                e,
                toEmail,
                "PASSWORD_RESET_CODE"
            );
        }
    }
    
    /**
     * Send password reset confirmation email
     * This is sent AFTER password has been changed, so we don't want to fail the operation
     * if email fails. Instead, we send it asynchronously and log failures.
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetConfirmation(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Password Has Been Reset");
            message.setText(buildConfirmationEmailContent(userName));

            log.debug("Attempting to send password reset confirmation email to: {}", toEmail);
            mailSender.send(message);
            log.info("✓ Password reset confirmation email sent successfully to: {}", toEmail);

        } catch (MailException e) {
            log.error("❌ Failed to send password reset confirmation email to {}", toEmail);
            log.error("SMTP Error details: {}", e.getMessage(), e);
            // Don't throw - this is a non-critical notification sent after the operation completed

        } catch (Exception e) {
            log.error("❌ Unexpected error sending confirmation email to {}", toEmail, e);
            // Don't throw - this is a non-critical notification
        }
    }
    
    /**
     * Send account deletion confirmation email
     * This is sent AFTER account has been deleted, so we don't want to fail the operation
     * if email fails. Instead, we send it asynchronously and log failures.
     */
    @Async("emailTaskExecutor")
    public void sendAccountDeletionConfirmation(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Account Has Been Deleted");
            message.setText(buildAccountDeletionEmailContent(userName));

            log.debug("Attempting to send account deletion confirmation email to: {}", toEmail);
            mailSender.send(message);
            log.info("✓ Account deletion confirmation email sent successfully to: {}", toEmail);

        } catch (MailException e) {
            log.error("❌ Failed to send account deletion confirmation email to {}", toEmail);
            log.error("SMTP Error details: {}", e.getMessage(), e);
            // Don't throw - this is a non-critical notification sent after the operation completed

        } catch (Exception e) {
            log.error("❌ Unexpected error sending account deletion email to {}", toEmail, e);
            // Don't throw - this is a non-critical notification
        }
    }
    
    private String buildResetCodeEmailContent(String userName, String resetCode) {
        return String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Your 6-digit reset code is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The E-commerce Team",
            userName, resetCode
        );
    }
    
    private String buildConfirmationEmailContent(String userName) {
        return String.format(
            "Hello %s,\n\n" +
            "Your password has been successfully reset.\n\n" +
            "If you did not make this change, please contact support immediately.\n\n" +
            "Best regards,\n" +
            "The E-commerce Team",
            userName
        );
    }
    
    private String buildAccountDeletionEmailContent(String userName) {
        return String.format(
            "Hello %s,\n\n" +
            "Your account has been successfully deleted.\n" +
            "All your personal data has been removed from our system.\n\n" +
            "We're sorry to see you go. If you change your mind, " +
            "you can always create a new account.\n\n" +
            "Thank you for being a part of our community.\n\n" +
            "Best regards,\n" +
            "The E-commerce Team",
            userName
        );
    }
}
