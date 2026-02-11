package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.CustomerTrustScore;

@Repository
public interface CustomerTrustScoreRepository extends MongoRepository<CustomerTrustScore, String> {
    Optional<CustomerTrustScore> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
