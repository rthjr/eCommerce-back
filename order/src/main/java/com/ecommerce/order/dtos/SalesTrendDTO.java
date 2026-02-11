package com.ecommerce.order.dtos;

import com.ecommerce.order.models.AnalyticsPeriod;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesTrendDTO {
    private AnalyticsPeriod period;
    private List<TrendDataPoint> trends;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendDataPoint {
        private LocalDate date;
        private String label;
        private BigDecimal revenue;
        private BigDecimal gmv;
        private Long orders;
        private Long activeUsers;
    }
}
