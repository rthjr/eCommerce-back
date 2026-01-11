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
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.CustomUserDetailsService.UserPrincipal;
import com.ecommerce.user.security.jwt.JwtTokenProvider;

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

    public JwtResponse register(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        user.getRoles().add("ROLE_USER");

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
                List.of("ROLE_USER")
        );

        return new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userInfo
        );
    }

    public JwtResponse login(LoginRequest loginRequest) {
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

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse.UserInfo userInfo = new JwtResponse.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getUsername(), // Using email as name for now
                roles
        );

        return new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userInfo
        );
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
