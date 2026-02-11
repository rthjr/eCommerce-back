package com.ecommerce.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReturnRequestDTO {
    private Long orderId;
    private String productId;
    private String reason;
    private List<String> photos;
}
