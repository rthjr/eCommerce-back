package com.ecommerce.user.services;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.user.models.OAuth2Token;
import com.ecommerce.user.models.OAuth2UserInfo;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.OAuth2TokenRepository;
import com.ecommerce.user.repository.OAuth2UserInfoRepository;
import com.ecommerce.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OAuth2ClientService {

    @Autowired
    private OAuth2TokenRepository oauth2TokenRepository;
    
    @Autowired
    private OAuth2UserInfoRepository oauth2UserInfoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2UserInfo processOAuth2User(String registrationId, Map<String, Object> attributes) {
        String provider = registrationId.toUpperCase();
        String providerId = extractProviderId(provider, attributes);
        String email = extractEmail(provider, attributes);
        String name = extractName(provider, attributes);
        String avatarUrl = extractAvatarUrl(provider, attributes);

        // Check if user already exists with this OAuth2 provider
        OAuth2UserInfo existingUserInfo = oauth2UserInfoRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        if (existingUserInfo != null) {
            return existingUserInfo;
        }

        // Check if user exists with same email
        User existingUser = userRepository.findByEmail(email).orElse(null);
        
        if (existingUser != null) {
            // Link OAuth2 to existing user
            return createOAuth2UserInfo(existingUser.getId(), provider, providerId, email, name, avatarUrl, attributes);
        }

        // Create new user using actual User model fields
        User newUser = new User();
        newUser.setName(name); // Use single name field
        newUser.setEmail(email);
        newUser.setEnabled(true);
        
        // Add default role to roles set
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        newUser.setRoles(roles);
        
        User savedUser = userRepository.save(newUser);

        // Create OAuth2 user info
        return createOAuth2UserInfo(savedUser.getId(), provider, providerId, email, name, avatarUrl, attributes);
    }
    
    private OAuth2UserInfo createOAuth2UserInfo(String userId, String provider, String providerId, 
                                               String email, String name, String avatarUrl, 
                                               Map<String, Object> attributes) {
        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setUserId(userId);
        userInfo.setProvider(provider);
        userInfo.setProviderId(providerId);
        userInfo.setEmail(email);
        userInfo.setName(name);
        userInfo.setAvatarUrl(avatarUrl);
        
        try {
            userInfo.setAttributes(objectMapper.writeValueAsString(attributes));
        } catch (JsonProcessingException e) {
            userInfo.setAttributes("{}");
        }
        
        return oauth2UserInfoRepository.save(userInfo);
    }

    public OAuth2Token storeOAuth2Token(String userId, String provider, String accessToken, 
            String refreshToken, String scope, int expiresIn) {
        OAuth2Token token = new OAuth2Token();
        token.setUserId(userId);
        token.setProvider(provider);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setTokenType("Bearer");
        token.setScope(scope);
        token.setExpiresAt(Instant.now().plusSeconds(expiresIn));
        
        return oauth2TokenRepository.save(token);
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "GOOGLE":
                return (String) attributes.get("sub");
            case "GITHUB":
                return String.valueOf(attributes.get("id"));
            case "FACEBOOK":
                return (String) attributes.get("id");
            default:
                return String.valueOf(attributes.get("id"));
        }
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        return (String) attributes.get("email");
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        return (String) attributes.get("name");
    }
    
    private String extractFirstName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.split(" ", 2);
        return parts[0];
    }
    
    private String extractLastName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "GOOGLE":
                return (String) attributes.get("picture");
            case "GITHUB":
                return (String) attributes.get("avatar_url");
            case "FACEBOOK":
                @SuppressWarnings("unchecked")
                Map<String, Object> pictureMap = (Map<String, Object>) attributes.get("picture");
                if (pictureMap != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) pictureMap.get("data");
                    return dataMap != null ? (String) dataMap.get("url") : null;
                }
                return null;
            default:
                return null;
        }
    }
}
