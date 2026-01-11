package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.OAuth2Token;

@Repository
public interface OAuth2TokenRepository extends MongoRepository<OAuth2Token, String> {
    Optional<OAuth2Token> findByUserIdAndProvider(String userId, String provider);
    void deleteByUserIdAndProvider(String userId, String provider);
}