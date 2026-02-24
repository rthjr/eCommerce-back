package com.ecommerce.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.dtos.ShippingConfigResponse;
import com.ecommerce.order.dtos.UpdateShippingConfigRequest;
import com.ecommerce.order.services.ShippingPricingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/shipping-config")
public class AdminShippingConfigController {
    private final ShippingPricingService shippingPricingService;

    @GetMapping
    public ResponseEntity<ShippingConfigResponse> getShippingConfig() {
        return ResponseEntity.ok(shippingPricingService.getShippingConfig());
    }

    @PutMapping
    public ResponseEntity<?> updateShippingConfig(@RequestBody(required = false) UpdateShippingConfigRequest request) {
        try {
            return ResponseEntity.ok(shippingPricingService.updateShippingConfig(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}
