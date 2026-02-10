package com.ecommerce.product.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewStatsResponse {
    private long totalReviews;
    private long answeredReviews;
    private long unansweredReviews;
    private long flaggedReviews;
    private double averageRating;
    private long rating5;
    private long rating4;
    private long rating3;
    private long rating2;
    private long rating1;
    private double responseRate;
}
