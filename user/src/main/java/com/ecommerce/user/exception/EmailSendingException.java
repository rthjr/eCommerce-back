package com.ecommerce.user.exception;

/**
 * Custom exception for email sending failures.
 * Provides additional context about the failed email operation.
 */
public class EmailSendingException extends RuntimeException {

    private final String recipientEmail;
    private final String emailType;

    public EmailSendingException(String message, String recipientEmail, String emailType) {
        super(message);
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
    }

    public EmailSendingException(String message, Throwable cause, String recipientEmail, String emailType) {
        super(message, cause);
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getEmailType() {
        return emailType;
    }
}
