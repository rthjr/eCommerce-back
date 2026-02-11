package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.CustomerTrustScoreDTO;
import com.ecommerce.user.dto.UpdateTrustScoreRequest;
import com.ecommerce.user.services.TrustScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trust-score")
@Slf4j
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    @GetMapping("/{userId}")
    public ResponseEntity<CustomerTrustScoreDTO> getTrustScore(@PathVariable String userId) {
        try {
            CustomerTrustScoreDTO trustScore = trustScoreService.getTrustScore(userId);
            return ResponseEntity.ok(trustScore);
        } catch (Exception e) {
            log.error("Error getting trust score for user {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/cod-limit")
    public ResponseEntity<Map<String, Object>> getCODLimit(@PathVariable String userId) {
        try {
            CustomerTrustScoreDTO trustScore = trustScoreService.getTrustScore(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", trustScore.getUserId());
            response.put("codLimit", trustScore.getCodLimit());
            response.put("score", trustScore.getScore());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting COD limit for user {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<CustomerTrustScoreDTO> updateTrustScore(
            @PathVariable String userId,
            @RequestBody UpdateTrustScoreRequest request) {
        try {
            CustomerTrustScoreDTO updatedScore = trustScoreService.updateTrustScore(userId, request);
            return ResponseEntity.ok(updatedScore);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for user {}: ", userId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating trust score for user {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
