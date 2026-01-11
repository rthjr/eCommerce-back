package com.ecommerce.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "oauth2")
@Data
public class OAuth2Config {
    
    private Google google;
    private Github github;
    private Facebook facebook;
    
    @Data
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String apiUri = "https://www.googleapis.com/oauth2/v4/userinfo";
    }
    
    @Data
    public static class Github {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String apiUri = "https://api.github.com/user";
    }
    
    @Data
    public static class Facebook {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String apiUri = "https://graph.facebook.com/me";
    }
}
