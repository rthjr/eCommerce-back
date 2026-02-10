package com.ecommerce.order.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "seller_bank_accounts")
@Data
@NoArgsConstructor
public class SellerBankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerId;

    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    private String routingNumber;
    private String swiftCode;

    private Boolean isPrimary = false;
    private Boolean isVerified = false;

    private LocalDateTime verifiedAt;
    private String verifiedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
