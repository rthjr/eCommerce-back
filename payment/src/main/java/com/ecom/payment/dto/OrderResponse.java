package com.ecom.payment.dto;

import lombok.Data;

@Data
public class OrderResponse {
    private String order_id;
    private String qr;
    private String md5;
    private String qr_image;
}
