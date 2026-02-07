package com.ecommerce.user.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Health indicator for email service.
 * Tests SMTP connection and reports status via /actuator/health endpoint.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    @Override
    public Health health() {
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;

                // Test SMTP connection
                mailSenderImpl.testConnection();

                log.debug("Email health check passed");
                return Health.up()
                        .withDetail("smtp.host", mailSenderImpl.getHost())
                        .withDetail("smtp.port", mailSenderImpl.getPort())
                        .withDetail("smtp.username", maskEmail(mailSenderImpl.getUsername()))
                        .build();
            }

            return Health.up().build();

        } catch (Exception e) {
            log.error("Email health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
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
