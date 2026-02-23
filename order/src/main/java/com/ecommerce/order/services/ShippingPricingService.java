package com.ecommerce.order.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.dtos.CambodiaProvinceShippingRateDTO;
import com.ecommerce.order.dtos.ShippingAddressDTO;
import com.ecommerce.order.dtos.ShippingConfigResponse;
import com.ecommerce.order.dtos.ShippingQuoteResponse;
import com.ecommerce.order.dtos.UpdateShippingConfigRequest;
import com.ecommerce.order.models.CambodiaProvinceShippingRate;
import com.ecommerce.order.models.ShippingConfig;
import com.ecommerce.order.repositories.CambodiaProvinceShippingRateRepository;
import com.ecommerce.order.repositories.ShippingConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShippingPricingService {
    private static final Set<String> CAMBODIA_COUNTRY_KEYS = Set.of("cambodia", "kh", "khm");
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final ShippingConfigRepository shippingConfigRepository;
    private final CambodiaProvinceShippingRateRepository provinceRateRepository;

    public ShippingQuoteResponse quoteShipping(ShippingAddressDTO shippingAddress) {
        ShippingConfig config = getOrCreateShippingConfig();
        BigDecimal defaultPrice = money(config.getDefaultShippingPrice());

        if (shippingAddress == null) {
            return new ShippingQuoteResponse(defaultPrice, "DEFAULT", null);
        }

        String countryKey = normalizeKey(shippingAddress.getCountry());
        String provinceKey = normalizeKey(shippingAddress.getState());

        if (CAMBODIA_COUNTRY_KEYS.contains(countryKey) && !provinceKey.isBlank()) {
            return provinceRateRepository.findFirstByProvinceKeyAndActiveTrue(provinceKey)
                    .map(rate -> new ShippingQuoteResponse(money(rate.getPrice()), "CAMBODIA_PROVINCE", rate.getProvince()))
                    .orElseGet(() -> new ShippingQuoteResponse(defaultPrice, "DEFAULT", null));
        }

        return new ShippingQuoteResponse(defaultPrice, "DEFAULT", null);
    }

    public ShippingConfigResponse getShippingConfig() {
        ShippingConfig config = getOrCreateShippingConfig();
        return mapConfigResponse(config);
    }

    @Transactional
    public ShippingConfigResponse updateShippingConfig(UpdateShippingConfigRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        BigDecimal defaultShippingPrice = money(request.getDefaultShippingPrice());
        validateNonNegative(defaultShippingPrice, "Default shipping price");

        List<CambodiaProvinceShippingRateDTO> incomingRates = request.getCambodiaProvinceRates() == null
                ? List.of()
                : request.getCambodiaProvinceRates();

        Set<String> provinceKeys = new HashSet<>();
        List<CambodiaProvinceShippingRate> ratesToSave = new ArrayList<>();

        for (CambodiaProvinceShippingRateDTO dto : incomingRates) {
            if (dto == null) {
                throw new IllegalArgumentException("Province rate entry is required");
            }
            String province = dto.getProvince() == null ? "" : dto.getProvince().trim();
            if (province.isBlank()) {
                throw new IllegalArgumentException("Province name is required");
            }
            String provinceKey = normalizeKey(province);
            if (!provinceKeys.add(provinceKey)) {
                throw new IllegalArgumentException("Duplicate province name: " + province);
            }

            BigDecimal price = money(dto.getPrice());
            validateNonNegative(price, "Province shipping price");

            CambodiaProvinceShippingRate rate = new CambodiaProvinceShippingRate();
            rate.setProvince(province);
            rate.setProvinceKey(provinceKey);
            rate.setPrice(price);
            rate.setActive(dto.getActive() == null ? Boolean.TRUE : dto.getActive());
            ratesToSave.add(rate);
        }

        ShippingConfig config = getOrCreateShippingConfig();
        config.setDefaultShippingPrice(defaultShippingPrice);
        shippingConfigRepository.save(config);

        provinceRateRepository.deleteAllInBatch();
        provinceRateRepository.flush();
        if (!ratesToSave.isEmpty()) {
            provinceRateRepository.saveAll(ratesToSave);
        }

        return mapConfigResponse(config);
    }

    private ShippingConfigResponse mapConfigResponse(ShippingConfig config) {
        List<CambodiaProvinceShippingRateDTO> rates = provinceRateRepository.findAllByOrderByProvinceAsc().stream()
                .map(rate -> new CambodiaProvinceShippingRateDTO(
                        rate.getId(),
                        rate.getProvince(),
                        money(rate.getPrice()),
                        Boolean.TRUE.equals(rate.getActive())))
                .toList();

        return new ShippingConfigResponse(money(config.getDefaultShippingPrice()), rates);
    }

    private ShippingConfig getOrCreateShippingConfig() {
        return shippingConfigRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            ShippingConfig config = new ShippingConfig();
            config.setDefaultShippingPrice(ZERO_MONEY);
            return shippingConfigRepository.save(config);
        });
    }

    private void validateNonNegative(BigDecimal value, String fieldName) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            return ZERO_MONEY;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
