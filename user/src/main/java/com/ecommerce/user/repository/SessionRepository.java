package com.ecommerce.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.UserSession;

/**
 * Repository for UserSession entity operations.
 */
@Repository
public interface SessionRepository extends MongoRepository<UserSession, String> {
    
    /**
     * Find all sessions for a user
     */
    List<UserSession> findByUserIdOrderByLastActivityDesc(String userId);
    
    /**
     * Find all active sessions for a user
     */
    List<UserSession> findByUserIdAndIsActiveOrderByLastActivityDesc(String userId, boolean isActive);
    
    /**
     * Find session by token
     */
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    /**
     * Find session by user ID and session token
     */
    Optional<UserSession> findByUserIdAndSessionToken(String userId, String sessionToken);
    
    /**
     * Delete all sessions for a user
     */
    @Query(value = "{ 'user_id': ?0 }", delete = true)
    void deleteByUserId(String userId);
    
    /**
     * Delete session by session token
     */
    @Query(value = "{ 'session_token': ?0 }", delete = true)
    void deleteBySessionToken(String sessionToken);
    
    /**
     * Count active sessions for a user
     */
    long countByUserIdAndIsActive(String userId, boolean isActive);
    
    /**
     * Find expired sessions
     */
    @Query("{ 'expires_at': { $lt: ?0 }, 'is_active': true }")
    List<UserSession> findExpiredSessions(LocalDateTime now);
    
    /**
     * Delete expired sessions
     */
    @Query(value = "{ 'expires_at': { $lt: ?0 } }", delete = true)
    void deleteExpiredSessions(LocalDateTime now);
}
