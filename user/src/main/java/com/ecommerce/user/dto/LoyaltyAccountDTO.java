package com.ecommerce.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccountDTO {
    private String id;
    private String userId;
    private Integer totalPoints;
    private Integer currentPoints;
    private String tier;
    private String tierUpdatedAt;
    private String createdAt;
    private String updatedAt;
}
