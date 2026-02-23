package com.ecommerce.order.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.models.CambodiaProvinceShippingRate;

public interface CambodiaProvinceShippingRateRepository extends JpaRepository<CambodiaProvinceShippingRate, Long> {
    Optional<CambodiaProvinceShippingRate> findFirstByProvinceKeyAndActiveTrue(String provinceKey);

    List<CambodiaProvinceShippingRate> findAllByOrderByProvinceAsc();
}
