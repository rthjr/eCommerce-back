package com.ecommerce.user.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Validates email configuration on application startup.
 * Only active when spring.mail.test-connection=true
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "spring.mail.test-connection",
    havingValue = "true",
    matchIfMissing = false
)
public class EmailConfigValidator {

    private final JavaMailSender mailSender;

    @PostConstruct
    public void validateEmailConfiguration() {
        log.info("Validating email configuration on startup...");

        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;

                log.info("Testing SMTP connection to {}:{}",
                         mailSenderImpl.getHost(),
                         mailSenderImpl.getPort());

                mailSenderImpl.testConnection();

                log.info("✓ Email configuration validated successfully");
                log.info("  SMTP Host: {}", mailSenderImpl.getHost());
                log.info("  SMTP Port: {}", mailSenderImpl.getPort());
                log.info("  SMTP Username: {}", maskEmail(mailSenderImpl.getUsername()));
            } else {
                log.warn("JavaMailSender is not an instance of JavaMailSenderImpl, skipping connection test");
            }

        } catch (Exception e) {
            log.error("❌ FAILED to connect to SMTP server on startup!", e);
            log.error("Email sending will NOT work. Please check your email configuration:");
            log.error("  1. Verify SMTP credentials in application.yml");
            log.error("  2. Check if Gmail App Password is still valid");
            log.error("  3. Ensure network allows connection to smtp.gmail.com:587");
            log.error("  4. Verify firewall settings");

            // Don't fail startup, but make it very obvious in logs
            log.error("⚠️  APPLICATION STARTED WITH BROKEN EMAIL CONFIGURATION ⚠️");
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(3, parts[0].length())) + "***@" + parts[1];
    }
}
