package com.ecommerce.order.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.models.ShippingConfig;

public interface ShippingConfigRepository extends JpaRepository<ShippingConfig, Long> {
    Optional<ShippingConfig> findFirstByOrderByIdAsc();
}
