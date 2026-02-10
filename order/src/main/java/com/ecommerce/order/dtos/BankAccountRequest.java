package com.ecommerce.order.dtos;

import lombok.Data;

@Data
public class BankAccountRequest {
    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    private String routingNumber;
    private String swiftCode;
    private Boolean isPrimary;
}
