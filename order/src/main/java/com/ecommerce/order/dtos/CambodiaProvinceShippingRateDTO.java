package com.ecommerce.order.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambodiaProvinceShippingRateDTO {
    private Long id;
    private String province;
    private BigDecimal price;
    private Boolean active;
}
