package com.ecommerce.user.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.user.dto.request.LoginRequest;
import com.ecommerce.user.dto.request.RegisterRequest;
import com.ecommerce.user.dto.response.JwtResponse;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.models.Role;
import com.ecommerce.user.models.User;
import com.ecommerce.user.models.UserSession;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.CustomUserDetailsService.UserPrincipal;
import com.ecommerce.user.security.jwt.JwtTokenProvider;
import com.ecommerce.user.services.SessionService.DeviceInfo;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private SessionService sessionService;
    
    // ThreadLocal to store current session token
    private static final ThreadLocal<String> currentSessionToken = new ThreadLocal<>();
    
    public static String getCurrentSessionToken() {
        return currentSessionToken.get();
    }
    
    public static void setCurrentSessionToken(String token) {
        currentSessionToken.set(token);
    }
    
    public static void clearCurrentSessionToken() {
        currentSessionToken.remove();
    }

    public JwtResponse register(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        user.getRoles().add("ROLE_CUSTOMER");

        User savedUser = userRepository.save(user);

        // Generate tokens
        String roles = String.join(",", savedUser.getRoles());

        String accessToken = tokenProvider.generateTokenFromUserId(
                savedUser.getId(), savedUser.getEmail(), roles);

        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        JwtResponse.UserInfo userInfo = new JwtResponse.UserInfo(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                List.of("ROLE_CUSTOMER")
        );

        return new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userInfo
        );
    }

    public JwtResponse login(LoginRequest loginRequest) {
        return login(loginRequest, null, null);
    }
    
    public JwtResponse login(LoginRequest loginRequest, String userAgent, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Delete old refresh token if exists
        refreshTokenService.deleteByUserId(userPrincipal.getId());

        String accessToken = tokenProvider.generateAccessToken(authentication);
        var refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
        
        // Create session for tracking
        String sessionToken = null;
        if (userAgent != null || ipAddress != null) {
            DeviceInfo deviceInfo = sessionService.parseUserAgent(userAgent != null ? userAgent : "");
            UserSession session = sessionService.createSession(
                    userPrincipal.getId(),
                    deviceInfo.device(),
                    deviceInfo.browser(),
                    deviceInfo.operatingSystem(),
                    ipAddress != null ? ipAddress : "Unknown",
                    null // Location would need a geo-IP service
            );
            sessionToken = session.getSessionToken();
            setCurrentSessionToken(sessionToken);
        }

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse.UserInfo userInfo = new JwtResponse.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getUsername(), // Using email as name for now
                roles
        );

        JwtResponse response = new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userInfo
        );
        
        // Include session token in response
        response.setSessionToken(sessionToken);
        
        return response;
    }

    public String logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            refreshTokenService.deleteByUserId(userPrincipal.getId());
            SecurityContextHolder.clearContext();
            return "User logged out successfully";
        }
        return "No user to logout";
    }

    public JwtResponse.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return new JwtResponse.UserInfo(
                    userPrincipal.getId(),
                    userPrincipal.getEmail(),
                    userPrincipal.getUsername(),
                    roles
            );
        }
        throw new ResourceNotFoundException("User not found");
    }
}
