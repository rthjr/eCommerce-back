package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.OAuth2UserInfo;

@Repository
public interface OAuth2UserInfoRepository extends MongoRepository<OAuth2UserInfo, String> {
    Optional<OAuth2UserInfo> findByProviderAndProviderId(String provider, String providerId);
    Optional<OAuth2UserInfo> findByUserId(String userId);
    void deleteByUserId(String userId);
}
