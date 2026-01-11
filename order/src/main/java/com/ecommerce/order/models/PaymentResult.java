package com.ecommerce.order.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private String paymentId;
    private String status;
    private String updateTime;
    private String emailAddress;
}
