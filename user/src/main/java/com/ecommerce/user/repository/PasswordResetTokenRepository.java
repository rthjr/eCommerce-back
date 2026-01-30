package com.ecommerce.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserId(String userId);
    
    @Query("{ 'token': ?0, 'used': false, 'expires_at': { $gt: ?1 } }")
    Optional<PasswordResetToken> findValidToken(String token, LocalDateTime now);
    
    void deleteByUserId(String userId);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
