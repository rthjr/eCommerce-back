package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a return request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequestDTO {
    private Long orderId;
    private String productId;
    private String sellerId;
    private String reason;
    private List<String> photos;
}
