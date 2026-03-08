package com.ecommerce.order.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_settings")
@Data
@NoArgsConstructor
public class StoreSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeName = "E-Shop";

    @Column(nullable = false)
    private String storeEmail = "contact@eshop.com";

    @Column(nullable = false)
    private String storePhone = "+1 234 567 8900";

    @Column(nullable = false)
    private String storeAddress = "123 Commerce Street, Business City, BC 12345";

    @Column(length = 1000)
    private String storeDescription = "Your one-stop shop for all your needs.";

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(nullable = false, length = 50)
    private String timezone = "America/New_York";
}
