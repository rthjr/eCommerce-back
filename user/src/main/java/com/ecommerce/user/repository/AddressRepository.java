package com.ecommerce.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.Address;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    
    List<Address> findByUserId(String userId);
    
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(String userId);
    
    Optional<Address> findByIdAndUserId(String id, String userId);
    
    @Query("{ 'userId': ?0, 'isDefault': true }")
    Optional<Address> findDefaultAddressByUserId(String userId);
    
    void deleteByIdAndUserId(String id, String userId);
    
    void deleteByUserId(String userId);
    
    long countByUserId(String userId);
    
    boolean existsByIdAndUserId(String id, String userId);
}
