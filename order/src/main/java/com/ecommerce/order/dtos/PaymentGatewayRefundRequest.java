package com.ecommerce.order.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayRefundRequest {
    @JsonProperty("refund_id")
    private String refundId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("payment_id")
    private String paymentId;

    private BigDecimal amount;
    private String currency;
    private String reason;
}
