package com.ecommerce.user.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.services.AddressService;
import com.ecommerce.user.services.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {
    
    private final AddressService addressService;
    private final JwtService jwtService;
    
    /**
     * Get all addresses for the authenticated user
     */
    @GetMapping
    public ResponseEntity<?> getUserAddresses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        List<AddressDTO> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Get a specific address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddressById(
            @PathVariable String addressId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        return addressService.getAddressById(addressId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get the default address for the authenticated user
     */
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        return addressService.getDefaultAddress(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new address
     */
    @PostMapping
    public ResponseEntity<?> createAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        try {
            AddressDTO createdAddress = addressService.createAddress(userId, addressDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
        } catch (Exception e) {
            log.error("Error creating address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create address", "message", e.getMessage()));
        }
    }
    
    /**
     * Update an existing address
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable String addressId,
            @Valid @RequestBody AddressDTO addressDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        return addressService.updateAddress(addressId, userId, addressDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable String addressId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        boolean deleted = addressService.deleteAddress(addressId, userId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Set an address as default
     */
    @PutMapping("/{addressId}/default")
    public ResponseEntity<?> setDefaultAddress(
            @PathVariable String addressId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
        }
        
        return addressService.setDefaultAddress(addressId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Extract user ID from JWT token
     */
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);
            log.debug("Extracted userId: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }
}
