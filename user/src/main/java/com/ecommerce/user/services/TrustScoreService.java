package com.ecommerce.user.services;

import com.ecommerce.user.dto.CustomerTrustScoreDTO;
import com.ecommerce.user.dto.UpdateTrustScoreRequest;
import com.ecommerce.user.models.CustomerTrustScore;
import com.ecommerce.user.repository.CustomerTrustScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustScoreService {
    private final CustomerTrustScoreRepository trustScoreRepository;

    // Score adjustment constants
    private static final double SUCCESSFUL_DELIVERY_BONUS = 5.0;
    private static final double FAILED_DELIVERY_PENALTY = -15.0;
    private static final double CANCELLATION_PENALTY = -10.0;

    // Score bounds
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;

    public CustomerTrustScoreDTO getTrustScore(String userId) {
        Optional<CustomerTrustScore> existingScore = trustScoreRepository.findByUserId(userId);

        if (existingScore.isPresent()) {
            return mapToDTO(existingScore.get());
        }

        // Create new trust score for first-time user
        CustomerTrustScore newScore = createNewTrustScore(userId);
        CustomerTrustScore saved = trustScoreRepository.save(newScore);
        return mapToDTO(saved);
    }

    public CustomerTrustScoreDTO updateTrustScore(String userId, UpdateTrustScoreRequest request) {
        CustomerTrustScore trustScore = trustScoreRepository.findByUserId(userId)
                .orElseGet(() -> createNewTrustScore(userId));

        // Increment total orders
        trustScore.setTotalOrders(trustScore.getTotalOrders() + 1);

        // Update score based on event
        String event = request.getEvent();
        double currentScore = trustScore.getScore();

        switch (event) {
            case "SUCCESSFUL_DELIVERY":
                trustScore.setSuccessfulOrders(trustScore.getSuccessfulOrders() + 1);
                currentScore += SUCCESSFUL_DELIVERY_BONUS;
                log.info("User {} had successful delivery, score increased by {}", userId, SUCCESSFUL_DELIVERY_BONUS);
                break;
            case "FAILED_DELIVERY":
                trustScore.setFailedOrders(trustScore.getFailedOrders() + 1);
                currentScore += FAILED_DELIVERY_PENALTY;
                log.info("User {} had failed delivery, score decreased by {}", userId, Math.abs(FAILED_DELIVERY_PENALTY));
                break;
            case "CANCELLATION":
                trustScore.setCancellations(trustScore.getCancellations() + 1);
                currentScore += CANCELLATION_PENALTY;
                log.info("User {} cancelled order, score decreased by {}", userId, Math.abs(CANCELLATION_PENALTY));
                break;
            default:
                log.warn("Unknown event type: {}", event);
                throw new IllegalArgumentException("Invalid event type: " + event);
        }

        // Ensure score stays within bounds
        currentScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, currentScore));
        trustScore.setScore(currentScore);

        // Recalculate COD limit based on new score
        double newCodLimit = calculateCODLimit(currentScore);
        trustScore.setCodLimit(newCodLimit);

        trustScore.setUpdatedAt(LocalDateTime.now());

        CustomerTrustScore saved = trustScoreRepository.save(trustScore);
        log.info("Updated trust score for user {}: score={}, codLimit={}", userId, saved.getScore(), saved.getCodLimit());

        return mapToDTO(saved);
    }

    public Double calculateCODLimit(Double score) {
        // COD Limit calculation based on trust score
        // Score ranges:
        // 90-100: 50,000 INR
        // 75-89:  25,000 INR
        // 60-74:  15,000 INR
        // 40-59:  10,000 INR
        // 20-39:  5,000 INR
        // 0-19:   2,000 INR (minimal trust)

        if (score >= 90.0) {
            return 50000.0;
        } else if (score >= 75.0) {
            return 25000.0;
        } else if (score >= 60.0) {
            return 15000.0;
        } else if (score >= 40.0) {
            return 10000.0;
        } else if (score >= 20.0) {
            return 5000.0;
        } else {
            return 2000.0;
        }
    }

    private CustomerTrustScore createNewTrustScore(String userId) {
        CustomerTrustScore trustScore = new CustomerTrustScore();
        trustScore.setUserId(userId);
        trustScore.setScore(100.0); // New users start with perfect score
        trustScore.setCodLimit(calculateCODLimit(100.0)); // Initial COD limit
        trustScore.setTotalOrders(0);
        trustScore.setSuccessfulOrders(0);
        trustScore.setFailedOrders(0);
        trustScore.setCancellations(0);
        trustScore.setCreatedAt(LocalDateTime.now());
        trustScore.setUpdatedAt(LocalDateTime.now());
        return trustScore;
    }

    private CustomerTrustScoreDTO mapToDTO(CustomerTrustScore trustScore) {
        CustomerTrustScoreDTO dto = new CustomerTrustScoreDTO();
        dto.setId(trustScore.getId());
        dto.setUserId(trustScore.getUserId());
        dto.setScore(trustScore.getScore());
        dto.setCodLimit(trustScore.getCodLimit());
        dto.setTotalOrders(trustScore.getTotalOrders());
        dto.setSuccessfulOrders(trustScore.getSuccessfulOrders());
        dto.setFailedOrders(trustScore.getFailedOrders());
        dto.setCancellations(trustScore.getCancellations());
        dto.setCreatedAt(trustScore.getCreatedAt() != null ? trustScore.getCreatedAt().toString() : null);
        dto.setUpdatedAt(trustScore.getUpdatedAt() != null ? trustScore.getUpdatedAt().toString() : null);
        return dto;
    }
}
