package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rejecting a return request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectReturnRequestDTO {
    private String reason;
}
