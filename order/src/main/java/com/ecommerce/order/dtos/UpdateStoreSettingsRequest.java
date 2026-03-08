package com.ecommerce.order.dtos;

import lombok.Data;

@Data
public class UpdateStoreSettingsRequest {
    private String storeName;
    private String storeEmail;
    private String storePhone;
    private String storeAddress;
    private String storeDescription;
    private String currency;
    private String timezone;
}
