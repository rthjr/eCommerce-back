package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.*;
import com.ecommerce.order.services.SellerFinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/financials")
public class SellerFinancialController {

    private final SellerFinancialService financialService;

    // Financial Overview
    @GetMapping("/overview")
    public ResponseEntity<SellerFinancialOverview> getFinancialOverview(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(financialService.getFinancialOverview(sellerId));
    }

    // Transactions
    @GetMapping("/transactions")
    public ResponseEntity<Page<SellerTransactionResponse>> getTransactions(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(financialService.getSellerTransactions(sellerId, page, size));
    }

    // Payouts
    @GetMapping("/payouts")
    public ResponseEntity<Page<SellerPayoutResponse>> getPayouts(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(financialService.getSellerPayouts(sellerId, page, size));
    }

    @GetMapping("/payouts/{id}")
    public ResponseEntity<SellerPayoutResponse> getPayoutById(@PathVariable Long id) {
        return financialService.getPayoutById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/payouts/request")
    public ResponseEntity<SellerPayoutResponse> requestPayout(
            @RequestHeader("X-User-Id") String sellerId) {
        return financialService.requestPayout(sellerId)
                .map(payout -> new ResponseEntity<>(payout, HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    // Bank Accounts
    @GetMapping("/bank-accounts")
    public ResponseEntity<List<SellerBankAccountResponse>> getBankAccounts(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(financialService.getBankAccounts(sellerId));
    }

    @PostMapping("/bank-accounts")
    public ResponseEntity<SellerBankAccountResponse> addBankAccount(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestBody BankAccountRequest request) {
        return new ResponseEntity<>(financialService.addBankAccount(sellerId, request), HttpStatus.CREATED);
    }

    @PutMapping("/bank-accounts/{id}/set-primary")
    public ResponseEntity<SellerBankAccountResponse> setPrimaryBankAccount(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long id) {
        return financialService.setPrimaryBankAccount(sellerId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<Void> deleteBankAccount(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long id) {
        return financialService.deleteBankAccount(sellerId, id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.badRequest().build();
    }

    // Revenue Report
    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(financialService.getRevenueReport(sellerId, startDate, endDate));
    }
}
