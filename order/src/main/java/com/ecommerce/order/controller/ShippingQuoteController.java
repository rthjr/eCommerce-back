package com.ecommerce.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.dtos.ShippingQuoteRequest;
import com.ecommerce.order.dtos.ShippingQuoteResponse;
import com.ecommerce.order.services.ShippingPricingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class ShippingQuoteController {
    private final ShippingPricingService shippingPricingService;

    @PostMapping("/quote")
    public ResponseEntity<?> quoteShipping(@RequestBody(required = false) ShippingQuoteRequest request) {
        if (request == null || request.getShippingAddress() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "shippingAddress is required"));
        }

        ShippingQuoteResponse response = shippingPricingService.quoteShipping(request.getShippingAddress());
        return ResponseEntity.ok(response);
    }
}
