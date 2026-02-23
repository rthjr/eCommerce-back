package com.ecommerce.order.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingQuoteResponse {
    private BigDecimal shippingPrice;
    private String matchedRuleType;
    private String matchedProvince;
}
