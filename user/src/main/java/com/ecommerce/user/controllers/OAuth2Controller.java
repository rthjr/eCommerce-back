package com.ecommerce.user.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.user.dto.response.MessageResponse;
import com.ecommerce.user.services.OAuth2ClientService;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    @Autowired
    private OAuth2ClientService oauth2ClientService;

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(oAuth2User.getAttributes());
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        Map<String, Object> providers = Map.of(
            "google", Map.of(
                "name", "Google",
                "authUrl", "/oauth2/authorization/google"
            ),
            "github", Map.of(
                "name", "GitHub", 
                "authUrl", "/oauth2/authorization/github"
            ),
            "facebook", Map.of(
                "name", "Facebook",
                "authUrl", "/oauth2/authorization/facebook"
            )
        );
        
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        // OAuth2 logout logic would go here
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
