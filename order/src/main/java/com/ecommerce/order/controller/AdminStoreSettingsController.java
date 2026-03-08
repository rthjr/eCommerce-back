package com.ecommerce.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.dtos.StoreSettingsResponse;
import com.ecommerce.order.dtos.UpdateStoreSettingsRequest;
import com.ecommerce.order.services.StoreSettingsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/store-settings")
public class AdminStoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    @GetMapping
    public ResponseEntity<StoreSettingsResponse> getStoreSettings() {
        return ResponseEntity.ok(storeSettingsService.getStoreSettings());
    }

    @PutMapping
    public ResponseEntity<StoreSettingsResponse> updateStoreSettings(
            @RequestBody UpdateStoreSettingsRequest request) {
        return ResponseEntity.ok(storeSettingsService.updateStoreSettings(request));
    }
}
