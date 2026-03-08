package com.ecommerce.order.services;

import org.springframework.stereotype.Service;

import com.ecommerce.order.dtos.StoreSettingsResponse;
import com.ecommerce.order.dtos.UpdateStoreSettingsRequest;
import com.ecommerce.order.models.StoreSettings;
import com.ecommerce.order.repositories.StoreSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;

    public StoreSettingsResponse getStoreSettings() {
        StoreSettings settings = storeSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> storeSettingsRepository.save(new StoreSettings()));
        return mapToResponse(settings);
    }

    public StoreSettingsResponse updateStoreSettings(UpdateStoreSettingsRequest request) {
        StoreSettings settings = storeSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> storeSettingsRepository.save(new StoreSettings()));

        if (request.getStoreName() != null) {
            settings.setStoreName(request.getStoreName());
        }
        if (request.getStoreEmail() != null) {
            settings.setStoreEmail(request.getStoreEmail());
        }
        if (request.getStorePhone() != null) {
            settings.setStorePhone(request.getStorePhone());
        }
        if (request.getStoreAddress() != null) {
            settings.setStoreAddress(request.getStoreAddress());
        }
        if (request.getStoreDescription() != null) {
            settings.setStoreDescription(request.getStoreDescription());
        }
        if (request.getCurrency() != null) {
            settings.setCurrency(request.getCurrency());
        }
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }

        StoreSettings saved = storeSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private StoreSettingsResponse mapToResponse(StoreSettings settings) {
        return new StoreSettingsResponse(
                settings.getStoreName(),
                settings.getStoreEmail(),
                settings.getStorePhone(),
                settings.getStoreAddress(),
                settings.getStoreDescription(),
                settings.getCurrency(),
                settings.getTimezone()
        );
    }
}
