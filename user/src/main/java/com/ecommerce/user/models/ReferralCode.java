package com.ecommerce.user.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "referral_codes")
public class ReferralCode {
    @Id
    private String id;

    @Indexed
    @Field(name = "user_id")
    private String userId;

    @Indexed(unique = true)
    @Field(name = "code")
    private String code;

    @Field(name = "usage_count")
    private Integer usageCount = 0;

    @Field(name = "max_usage")
    private Integer maxUsage = 100;

    @Field(name = "created_at")
    private LocalDateTime createdAt;
}
