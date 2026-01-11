package com.ecommerce.user.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.user.exception.TokenRefreshException;
import com.ecommerce.user.models.RefreshToken;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.RefreshTokenRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.jwt.JwtTokenProvider;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private static final long REFRESH_TOKEN_DURATION_MS = 2592000000L; // 30 days

    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS));
        refreshToken.setCreatedAt(java.time.LocalDateTime.now());
        
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token is not in database!"));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public String refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = findByToken(refreshTokenString);
        verifyExpiration(refreshToken);
        
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new TokenRefreshException(refreshTokenString, "User not found"));
        
        String roles = String.join(",", user.getRoles());
        
        return tokenProvider.generateTokenFromUserId(user.getId(), user.getEmail(), roles);
    }

    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
}
