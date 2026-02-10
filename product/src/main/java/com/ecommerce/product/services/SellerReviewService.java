package com.ecommerce.product.services;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.models.ProductReview;
import com.ecommerce.product.models.ProductReview.ReviewStatus;
import com.ecommerce.product.repositories.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerReviewService {

    private final ProductReviewRepository reviewRepository;

    public Page<SellerReviewResponse> getSellerReviews(String sellerId, int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "oldest" -> Sort.by("date").ascending();
            case "highest" -> Sort.by("rating").descending();
            case "lowest" -> Sort.by("rating").ascending();
            default -> Sort.by("date").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        return reviewRepository.findByProductSellerId(sellerId, pageable).map(this::mapToSellerResponse);
    }

    public List<SellerReviewResponse> getAllSellerReviews(String sellerId) {
        return reviewRepository.findAllByProductSellerId(sellerId)
                .stream()
                .map(this::mapToSellerResponse)
                .collect(Collectors.toList());
    }

    public List<SellerReviewResponse> getUnansweredReviews(String sellerId) {
        return reviewRepository.findUnansweredByProductSellerId(sellerId)
                .stream()
                .map(this::mapToSellerResponse)
                .collect(Collectors.toList());
    }

    public List<SellerReviewResponse> getFlaggedReviews(String sellerId) {
        return reviewRepository.findFlaggedByProductSellerId(sellerId)
                .stream()
                .map(this::mapToSellerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<SellerReviewResponse> respondToReview(Long reviewId, String sellerId, SellerResponseRequest request) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getProduct().getSellerId().equals(sellerId))
                .map(review -> {
                    review.setSellerResponse(request.getResponse());
                    review.setSellerResponseDate(LocalDateTime.now());
                    return mapToSellerResponse(reviewRepository.save(review));
                });
    }

    @Transactional
    public Optional<SellerReviewResponse> updateResponse(Long reviewId, String sellerId, SellerResponseRequest request) {
        return respondToReview(reviewId, sellerId, request);
    }

    @Transactional
    public Optional<SellerReviewResponse> deleteResponse(Long reviewId, String sellerId) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getProduct().getSellerId().equals(sellerId))
                .map(review -> {
                    review.setSellerResponse(null);
                    review.setSellerResponseDate(null);
                    return mapToSellerResponse(reviewRepository.save(review));
                });
    }

    @Transactional
    public Optional<SellerReviewResponse> flagReview(Long reviewId, String sellerId, String reason) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getProduct().getSellerId().equals(sellerId))
                .map(review -> {
                    review.setIsFlagged(true);
                    review.setFlagReason(reason);
                    review.setFlaggedAt(LocalDateTime.now());
                    review.setStatus(ReviewStatus.FLAGGED);
                    return mapToSellerResponse(reviewRepository.save(review));
                });
    }

    public ReviewStatsResponse getReviewStats(String sellerId) {
        List<ProductReview> reviews = reviewRepository.findAllByProductSellerId(sellerId);
        
        long total = reviews.size();
        long answered = reviews.stream().filter(r -> r.getSellerResponse() != null).count();
        long unanswered = total - answered;
        long flagged = reviews.stream().filter(ProductReview::getIsFlagged).count();
        
        Double avgRating = reviewRepository.getAverageRatingBySellerId(sellerId);
        double averageRating = avgRating != null ? avgRating : 0.0;
        
        // Rating distribution
        long rating5 = reviews.stream().filter(r -> r.getRating() == 5).count();
        long rating4 = reviews.stream().filter(r -> r.getRating() == 4).count();
        long rating3 = reviews.stream().filter(r -> r.getRating() == 3).count();
        long rating2 = reviews.stream().filter(r -> r.getRating() == 2).count();
        long rating1 = reviews.stream().filter(r -> r.getRating() == 1).count();
        
        double responseRate = total > 0 ? (double) answered / total * 100 : 0;
        
        return new ReviewStatsResponse(
                total, answered, unanswered, flagged, averageRating,
                rating5, rating4, rating3, rating2, rating1, responseRate
        );
    }

    private SellerReviewResponse mapToSellerResponse(ProductReview review) {
        SellerReviewResponse response = new SellerReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setProductImage(review.getProduct().getImageUrl());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setUserId(review.getUserId());
        response.setUserName(review.getUser());
        response.setDate(review.getDate());
        response.setVerifiedPurchase(review.getVerifiedPurchase());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setSellerResponse(review.getSellerResponse());
        response.setSellerResponseDate(review.getSellerResponseDate());
        response.setIsFlagged(review.getIsFlagged());
        response.setFlagReason(review.getFlagReason());
        response.setStatus(review.getStatus());
        return response;
    }
}
