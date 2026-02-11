package com.ecommerce.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrustScoreRequest {
    private String event; // SUCCESSFUL_DELIVERY, FAILED_DELIVERY, CANCELLATION
    private String orderId;
}
