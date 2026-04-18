package com.ecom.payment.dto;

import lombok.Data;

@Data
public class OrderStatusResponse {
    private String status;
    private String message;
    private String order_id;
}
