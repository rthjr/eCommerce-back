package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleDeliveryDTO {
    private LocalDate preferredDate;
    private LocalTime preferredTime;
}
