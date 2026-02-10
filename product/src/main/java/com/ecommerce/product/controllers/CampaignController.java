package com.ecommerce.product.controllers;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.services.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestBody CampaignRequest request) {
        return new ResponseEntity<>(campaignService.createCampaign(sellerId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getSellerCampaigns(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(campaignService.getSellerCampaigns(sellerId));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<CampaignResponse>> getSellerCampaignsPaged(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(campaignService.getSellerCampaignsPaged(sellerId, page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(campaignService.getActiveCampaigns(sellerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        return campaignService.getCampaignById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable Long id,
            @RequestBody CampaignRequest request) {
        return campaignService.updateCampaign(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CampaignResponse> activateCampaign(@PathVariable Long id) {
        return campaignService.activateCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<CampaignResponse> pauseCampaign(@PathVariable Long id) {
        return campaignService.pauseCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/schedule")
    public ResponseEntity<CampaignResponse> scheduleCampaign(@PathVariable Long id) {
        return campaignService.scheduleCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<CampaignResponse> endCampaign(@PathVariable Long id) {
        return campaignService.endCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        return campaignService.deleteCampaign(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Coupon endpoints
    @PostMapping("/coupons")
    public ResponseEntity<CouponCodeResponse> createCouponCode(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestBody CouponCodeRequest request) {
        return new ResponseEntity<>(campaignService.createCouponCode(sellerId, request), HttpStatus.CREATED);
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponCodeResponse>> getSellerCoupons(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(campaignService.getSellerCoupons(sellerId));
    }

    @GetMapping("/coupons/validate/{code}")
    public ResponseEntity<CouponCodeResponse> validateCoupon(@PathVariable String code) {
        return campaignService.validateCouponCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/coupons/use/{code}")
    public ResponseEntity<CouponCodeResponse> useCoupon(@PathVariable String code) {
        return campaignService.useCouponCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/coupons/{id}/deactivate")
    public ResponseEntity<Void> deactivateCoupon(@PathVariable Long id) {
        return campaignService.deactivateCoupon(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    // Stats and performance
    @GetMapping("/stats")
    public ResponseEntity<CampaignStatsResponse> getCampaignStats(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(campaignService.getCampaignStats(sellerId));
    }

    @PostMapping("/{id}/track-view")
    public ResponseEntity<Void> trackView(@PathVariable Long id) {
        campaignService.trackCampaignView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/track-click")
    public ResponseEntity<Void> trackClick(@PathVariable Long id) {
        campaignService.trackCampaignClick(id);
        return ResponseEntity.ok().build();
    }
}
