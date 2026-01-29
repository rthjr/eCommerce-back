package com.ecommerce.user.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.repository.AddressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {
    
    private final AddressRepository addressRepository;
    
    /**
     * Get all addresses for a user
     */
    public List<AddressDTO> getUserAddresses(String userId) {
        log.info("Fetching addresses for user: {}", userId);
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific address by ID
     */
    public Optional<AddressDTO> getAddressById(String addressId, String userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .map(this::mapToDTO);
    }
    
    /**
     * Get the default address for a user
     */
    public Optional<AddressDTO> getDefaultAddress(String userId) {
        return addressRepository.findDefaultAddressByUserId(userId)
                .map(this::mapToDTO);
    }
    
    /**
     * Create a new address
     */
    @Transactional
    public AddressDTO createAddress(String userId, AddressDTO addressDTO) {
        log.info("Creating address for user: {}", userId);
        
        // If this is the first address or marked as default, ensure it's the only default
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            clearDefaultAddresses(userId);
        }
        
        // If this is the first address, make it default
        if (addressRepository.countByUserId(userId) == 0) {
            addressDTO.setIsDefault(true);
        }
        
        Address address = mapToEntity(addressDTO);
        address.setUserId(userId);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        
        // Default country to Cambodia
        if (address.getCountry() == null || address.getCountry().isEmpty()) {
            address.setCountry("Cambodia");
        }
        
        Address savedAddress = addressRepository.save(address);
        log.info("Address created with ID: {}", savedAddress.getId());
        
        return mapToDTO(savedAddress);
    }
    
    /**
     * Update an existing address
     */
    @Transactional
    public Optional<AddressDTO> updateAddress(String addressId, String userId, AddressDTO addressDTO) {
        log.info("Updating address {} for user: {}", addressId, userId);
        
        return addressRepository.findByIdAndUserId(addressId, userId)
                .map(existingAddress -> {
                    // If setting as default, clear other defaults
                    if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() && 
                        (existingAddress.getIsDefault() == null || !existingAddress.getIsDefault())) {
                        clearDefaultAddresses(userId);
                    }
                    
                    updateEntityFromDTO(existingAddress, addressDTO);
                    existingAddress.setUpdatedAt(LocalDateTime.now());
                    
                    Address savedAddress = addressRepository.save(existingAddress);
                    return mapToDTO(savedAddress);
                });
    }
    
    /**
     * Delete an address
     */
    @Transactional
    public boolean deleteAddress(String addressId, String userId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        Optional<Address> addressOpt = addressRepository.findByIdAndUserId(addressId, userId);
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            boolean wasDefault = address.getIsDefault() != null && address.getIsDefault();
            
            addressRepository.delete(address);
            
            // If deleted address was default, set another one as default
            if (wasDefault) {
                List<Address> remainingAddresses = addressRepository.findByUserId(userId);
                if (!remainingAddresses.isEmpty()) {
                    Address newDefault = remainingAddresses.get(0);
                    newDefault.setIsDefault(true);
                    newDefault.setUpdatedAt(LocalDateTime.now());
                    addressRepository.save(newDefault);
                }
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Set an address as default
     */
    @Transactional
    public Optional<AddressDTO> setDefaultAddress(String addressId, String userId) {
        log.info("Setting address {} as default for user: {}", addressId, userId);
        
        return addressRepository.findByIdAndUserId(addressId, userId)
                .map(address -> {
                    clearDefaultAddresses(userId);
                    address.setIsDefault(true);
                    address.setUpdatedAt(LocalDateTime.now());
                    Address savedAddress = addressRepository.save(address);
                    return mapToDTO(savedAddress);
                });
    }
    
    /**
     * Clear all default addresses for a user
     */
    private void clearDefaultAddresses(String userId) {
        addressRepository.findByUserId(userId)
                .stream()
                .filter(a -> a.getIsDefault() != null && a.getIsDefault())
                .forEach(a -> {
                    a.setIsDefault(false);
                    a.setUpdatedAt(LocalDateTime.now());
                    addressRepository.save(a);
                });
    }
    
    /**
     * Map Address entity to DTO
     */
    private AddressDTO mapToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .label(address.getLabel())
                .isDefault(address.getIsDefault())
                .firstName(address.getFirstName())
                .lastName(address.getLastName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .village(address.getVillage())
                .commune(address.getCommune())
                .district(address.getDistrict())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .additionalInfo(address.getAdditionalInfo())
                .build();
    }
    
    /**
     * Map DTO to Address entity
     */
    private Address mapToEntity(AddressDTO dto) {
        return Address.builder()
                .label(dto.getLabel())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .street(dto.getStreet())
                .village(dto.getVillage())
                .commune(dto.getCommune())
                .district(dto.getDistrict())
                .province(dto.getProvince())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry() != null ? dto.getCountry() : "Cambodia")
                .additionalInfo(dto.getAdditionalInfo())
                .build();
    }
    
    /**
     * Update entity from DTO
     */
    private void updateEntityFromDTO(Address address, AddressDTO dto) {
        if (dto.getLabel() != null) address.setLabel(dto.getLabel());
        if (dto.getIsDefault() != null) address.setIsDefault(dto.getIsDefault());
        if (dto.getFirstName() != null) address.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) address.setLastName(dto.getLastName());
        if (dto.getPhone() != null) address.setPhone(dto.getPhone());
        if (dto.getStreet() != null) address.setStreet(dto.getStreet());
        if (dto.getVillage() != null) address.setVillage(dto.getVillage());
        if (dto.getCommune() != null) address.setCommune(dto.getCommune());
        if (dto.getDistrict() != null) address.setDistrict(dto.getDistrict());
        if (dto.getProvince() != null) address.setProvince(dto.getProvince());
        if (dto.getPostalCode() != null) address.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry());
        if (dto.getAdditionalInfo() != null) address.setAdditionalInfo(dto.getAdditionalInfo());
    }
}
