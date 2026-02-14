package com.ecommerce.product.controllers;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.services.SellerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/reviews")
public class SellerReviewController {

    private final SellerReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<SellerReviewResponse>> getSellerReviews(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(reviewService.getSellerReviews(sellerId, page, size, sortBy));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SellerReviewResponse>> getAllSellerReviews(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(reviewService.getAllSellerReviews(sellerId));
    }

    @GetMapping("/unanswered")
    public ResponseEntity<List<SellerReviewResponse>> getUnansweredReviews(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(reviewService.getUnansweredReviews(sellerId));
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<SellerReviewResponse>> getFlaggedReviews(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(reviewService.getFlaggedReviews(sellerId));
    }

    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsResponse> getReviewStats(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(reviewService.getReviewStats(sellerId));
    }

    @PostMapping("/{reviewId}/respond")
    public ResponseEntity<SellerReviewResponse> respondToReview(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId,
            @RequestBody SellerResponseRequest request) {
        return reviewService.respondToReview(reviewId, sellerId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reviewId}/respond")
    public ResponseEntity<SellerReviewResponse> updateResponse(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId,
            @RequestBody SellerResponseRequest request) {
        return reviewService.updateResponse(reviewId, sellerId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{reviewId}/respond")
    public ResponseEntity<SellerReviewResponse> deleteResponse(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId) {
        return reviewService.deleteResponse(reviewId, sellerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reviewId}/flag")
    public ResponseEntity<SellerReviewResponse> flagReview(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Inappropriate content");
        return reviewService.flagReview(reviewId, sellerId, reason)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reviewId}/hide")
    public ResponseEntity<SellerReviewResponse> hideReview(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId) {
        return reviewService.hideReview(reviewId, sellerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reviewId}/publish")
    public ResponseEntity<SellerReviewResponse> publishReview(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long reviewId) {
        return reviewService.publishReview(reviewId, sellerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
