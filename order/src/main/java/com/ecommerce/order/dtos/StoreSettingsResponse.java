package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreSettingsResponse {
    private String storeName;
    private String storeEmail;
    private String storePhone;
    private String storeAddress;
    private String storeDescription;
    private String currency;
    private String timezone;
}
