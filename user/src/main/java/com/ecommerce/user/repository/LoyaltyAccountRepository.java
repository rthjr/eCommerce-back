package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.LoyaltyAccount;

@Repository
public interface LoyaltyAccountRepository extends MongoRepository<LoyaltyAccount, String> {
    Optional<LoyaltyAccount> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
