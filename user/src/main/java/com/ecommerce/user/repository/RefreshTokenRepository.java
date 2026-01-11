package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.RefreshToken;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserId(String userId);
    
    @Query(value = "{ 'user_id': ?0 }", delete = true)
    void deleteByUserId(String userId);
}
