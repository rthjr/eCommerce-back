package com.ecommerce.product.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignStatsResponse {
    private long totalCampaigns;
    private long activeCampaigns;
    private long scheduledCampaigns;
    private long endedCampaigns;
    private int totalViews;
    private int totalClicks;
    private int totalConversions;
    private BigDecimal totalRevenue;
    private double conversionRate;
}
