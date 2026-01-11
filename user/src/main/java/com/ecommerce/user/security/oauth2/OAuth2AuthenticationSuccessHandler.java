package com.ecommerce.user.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecommerce.user.models.OAuth2UserInfo;
import com.ecommerce.user.security.jwt.JwtTokenProvider;
import com.ecommerce.user.services.OAuth2ClientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OAuth2ClientService oauth2ClientService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = getRegistrationId(request);
        
        try {
            // Process OAuth2 user and create/link account
            OAuth2UserInfo userInfo = oauth2ClientService.processOAuth2User(registrationId, oAuth2User.getAttributes());
            
            // Generate JWT token using correct method name
            String token = jwtTokenProvider.generateTokenFromUserId(userInfo.getEmail(), userInfo.getEmail(), "ROLE_USER");
            
            // Redirect to frontend with token
            String redirectUrl = "http://localhost:3000/oauth2/redirect?token=" + token;
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            // Redirect to error page
            response.sendRedirect("http://localhost:3000/oauth2/redirect?error=authentication_failed");
        }
    }
    
    private String getRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/oauth2/code/")) {
            return requestUri.substring(requestUri.lastIndexOf("/") + 1);
        }
        return "unknown";
    }
}