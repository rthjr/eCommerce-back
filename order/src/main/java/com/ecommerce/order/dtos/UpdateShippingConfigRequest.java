package com.ecommerce.order.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShippingConfigRequest {
    private BigDecimal defaultShippingPrice;
    private List<CambodiaProvinceShippingRateDTO> cambodiaProvinceRates;
}
