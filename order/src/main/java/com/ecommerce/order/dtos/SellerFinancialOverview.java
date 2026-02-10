package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerFinancialOverview {
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private BigDecimal totalPaid;
    private BigDecimal pendingPayout;
    private BigDecimal monthlyRevenue;
}
