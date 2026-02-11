package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordFailedDeliveryDTO {
    private String riderId;
    private String failureReason;
    private String notes;
}
