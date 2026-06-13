package com.ecom.payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusMessage {
    private String orderId;
    private String status;
    private String paidAt;
}
