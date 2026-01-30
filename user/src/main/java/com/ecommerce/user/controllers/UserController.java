package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.DeleteAccountRequest;
import com.ecommerce.user.dto.PasswordChangeRequest;
import com.ecommerce.user.dto.ProfileUpdateRequest;
import com.ecommerce.user.dto.UserDataExport;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.services.AccountService;
import com.ecommerce.user.services.JwtService;
import com.ecommerce.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

	private final UserService userService;
	private final JwtService jwtService;
	private final AccountService accountService;

	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return new ResponseEntity<>(userService.fetchAllUsers(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUser(@PathVariable String id) {

		log.trace("This is TRACE level - Very detailed logs");
		log.debug("This is DEBUG level - Used for development debugging");
		log.info("This is INFO level - General system information");
		log.warn("This is WARN level - Something might be wrong");
		log.error("This is ERROR level - Something failed");

		return userService.fetchUser(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {
		userService.addUser(userRequest);
		return ResponseEntity.ok("User added successfully");
	}

	@PutMapping("/{id}")
	public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody UserRequest updateUserRequest) {
		boolean updated = userService.updateUser(id, updateUserRequest);
		if (updated)
			return ResponseEntity.ok("User updated successfully");
		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		boolean deleted = userService.deleteUser(id);
		return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
	
	/**
	 * Get current user's profile
	 */
	@GetMapping("/profile")
	public ResponseEntity<?> getCurrentUserProfile(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		
		String userId = extractUserIdFromToken(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
		}
		
		return userService.fetchUser(userId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * Update current user's profile
	 */
	@PutMapping("/profile")
	public ResponseEntity<?> updateProfile(
			@Valid @RequestBody ProfileUpdateRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		
		String userId = extractUserIdFromToken(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
		}
		
		try {
			return userService.updateProfile(userId, request)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		} catch (RuntimeException e) {
			log.error("Error updating profile: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Profile update failed", "message", e.getMessage()));
		}
	}
	
	/**
	 * Change current user's password
	 */
	@PutMapping("/profile/password")
	public ResponseEntity<?> changePassword(
			@Valid @RequestBody PasswordChangeRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		
		String userId = extractUserIdFromToken(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
		}
		
		try {
			boolean changed = userService.changePassword(userId, request);
			if (changed) {
				return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
			}
			return ResponseEntity.notFound().build();
		} catch (RuntimeException e) {
			log.error("Error changing password: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Password change failed", "message", e.getMessage()));
		}
	}
	
	/**
	 * Delete current user's account
	 */
	@DeleteMapping("/profile")
	public ResponseEntity<?> deleteAccount(
			@Valid @RequestBody DeleteAccountRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		
		String userId = extractUserIdFromToken(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
		}
		
		try {
			accountService.deleteAccount(userId, request);
			return ResponseEntity.ok(Map.of(
					"message", "Account deleted successfully",
					"success", true
			));
		} catch (IllegalArgumentException e) {
			log.warn("Account deletion failed for user {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Account deletion failed", "message", e.getMessage(), "success", false));
		} catch (Exception e) {
			log.error("Error deleting account for user {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Account deletion failed", "message", "An unexpected error occurred", "success", false));
		}
	}
	
	/**
	 * Export current user's data (GDPR compliance)
	 */
	@GetMapping("/profile/export")
	public ResponseEntity<?> exportUserData(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		
		String userId = extractUserIdFromToken(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized", "message", "Valid authentication token required"));
		}
		
		try {
			UserDataExport exportData = accountService.exportUserData(userId);
			return ResponseEntity.ok(exportData);
		} catch (Exception e) {
			log.error("Error exporting data for user {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Data export failed", "message", "Failed to export user data"));
		}
	}
	
	/**
	 * Extract user ID from JWT token
	 */
	private String extractUserIdFromToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.warn("Missing or invalid Authorization header");
			return null;
		}
		
		try {
			String token = authHeader.substring(7);
			String userId = jwtService.extractUserId(token);
			log.debug("Extracted userId: {}", userId);
			return userId;
		} catch (Exception e) {
			log.error("Failed to extract user ID from token: {}", e.getMessage());
			return null;
		}
	}
}
