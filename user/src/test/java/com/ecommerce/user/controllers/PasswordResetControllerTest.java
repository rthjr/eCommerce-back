package com.ecommerce.user.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.user.BaseIntegrationTest;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.repository.PasswordResetTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AutoConfigureMockMvc
class PasswordResetControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("oldPassword123"));
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);
    }

    // --- Forgot Password Tests ---

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void forgotPassword_WithNonExistentEmail_ShouldStillReturnSuccess() throws Exception {
        // Security best practice: don't reveal if email exists
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void forgotPassword_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "invalid-email");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Validate Reset Token Tests ---

    @Test
    void validateResetToken_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Invalid tokens return 400 Bad Request with valid=false
        mockMvc.perform(get("/api/auth/validate-reset-token")
                .param("token", "invalid-token-12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validateResetToken_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/auth/validate-reset-token"))
                .andExpect(status().isBadRequest());
    }

    // --- Reset Password Tests ---

    @Test
    void resetPassword_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "invalid-token");
        request.put("newPassword", "newPassword123");
        request.put("confirmPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_WithMismatchedPasswords_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "some-token");
        request.put("newPassword", "newPassword123");
        request.put("confirmPassword", "differentPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "some-token");
        request.put("newPassword", "short");
        request.put("confirmPassword", "short");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_WithEmptyFields_ShouldReturnBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "");
        request.put("newPassword", "");
        request.put("confirmPassword", "");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
