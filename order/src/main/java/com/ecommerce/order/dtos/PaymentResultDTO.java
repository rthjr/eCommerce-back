package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDTO {
    private String paymentId;
    private String status;
    private String updateTime;
    private String emailAddress;
}
