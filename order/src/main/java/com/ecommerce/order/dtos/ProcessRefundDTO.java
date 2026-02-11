package com.ecommerce.order.dtos;

import com.ecommerce.order.models.RefundMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessRefundDTO {
    private RefundMethod method;
}
