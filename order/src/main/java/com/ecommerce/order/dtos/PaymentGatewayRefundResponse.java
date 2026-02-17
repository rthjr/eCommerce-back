package com.ecommerce.order.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayRefundResponse {
    @JsonProperty("refund_id")
    private String refundId;

    private String status;

    @JsonProperty("provider_refund_id")
    private String providerRefundId;

    @JsonProperty("processed_at")
    private String processedAt;

    private String message;
}
