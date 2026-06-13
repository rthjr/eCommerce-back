package com.ecommerce.order.dtos;

import lombok.Data;

@Data
public class PaymentStatusMessage {
    private String orderId;
    private String status;
    private String paidAt;
}
