package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.services.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loyalty")
@Slf4j
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/account")
    public ResponseEntity<LoyaltyAccountDTO> getLoyaltyAccount(
            @RequestHeader("X-User-ID") String userId) {
        try {
            LoyaltyAccountDTO account = loyaltyService.getOrCreateLoyaltyAccount(userId);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Error getting loyalty account: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<PointTransactionDTO>> getTransactions(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PointTransactionDTO> transactions = loyaltyService.getTransactions(userId, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting transactions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/redeem")
    public ResponseEntity<LoyaltyAccountDTO> redeemPoints(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody RedeemPointsRequest request) {
        try {
            LoyaltyAccountDTO account = loyaltyService.redeemPoints(userId, request.getPoints());
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            log.error("Error redeeming points: ", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error redeeming points: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/referral-code")
    public ResponseEntity<ReferralCodeDTO> getReferralCode(
            @RequestHeader("X-User-ID") String userId) {
        try {
            ReferralCodeDTO code = loyaltyService.getReferralCode(userId);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            log.error("Error getting referral code: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/apply-referral")
    public ResponseEntity<LoyaltyAccountDTO> applyReferralCode(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody ApplyReferralRequest request) {
        try {
            LoyaltyAccountDTO account = loyaltyService.applyReferralCode(userId, request.getReferralCode());
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            log.error("Error applying referral code: ", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error applying referral code: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
