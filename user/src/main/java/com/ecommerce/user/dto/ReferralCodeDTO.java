package com.ecommerce.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCodeDTO {
    private String id;
    private String userId;
    private String code;
    private Integer usageCount;
    private Integer maxUsage;
    private String createdAt;
}
