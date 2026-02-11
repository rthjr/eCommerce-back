package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.ReferralCode;

@Repository
public interface ReferralCodeRepository extends MongoRepository<ReferralCode, String> {
    Optional<ReferralCode> findByUserId(String userId);
    Optional<ReferralCode> findByCode(String code);
    boolean existsByCode(String code);
}
