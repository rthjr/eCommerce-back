package com.ecom.payment.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Double amount;
    private String user_id;
    private String user_role;
}
