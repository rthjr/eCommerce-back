package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectReturnRequestDTO {
    private String reason;
}
