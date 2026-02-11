package com.ecommerce.order.dtos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodBreakdownDTO {
    private List<PaymentMethodData> paymentMethods;
    private BigDecimal totalRevenue;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentMethodData {
        private String paymentMethod;
        private BigDecimal revenue;
        private Long orders;
        private BigDecimal percentageOfTotal;
        private BigDecimal averageOrderValue;
    }
}
