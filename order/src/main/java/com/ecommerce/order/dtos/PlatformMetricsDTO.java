package com.ecommerce.order.dtos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformMetricsDTO {
    private Long id;
    private LocalDate date;
    private BigDecimal gmv;
    private BigDecimal revenue;
    private BigDecimal commission;
    private BigDecimal fees;
    private Long totalOrders;
    private Long activeUsers;
    private Long newUsers;
    private Long pageViews;
}
