package com.ecommerce.order.controller;

import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.*;
import com.ecommerce.order.services.ReturnRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/returns")
public class ReturnRefundController {
    private final ReturnRefundService returnRefundService;

    @PostMapping
    public ResponseEntity<ReturnRequestDTO> createReturnRequest(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CreateReturnRequestDTO request) {
        return returnRefundService.createReturnRequest(
                        userId,
                        request.getOrderId(),
                        request.getProductId(),
                        request.getSellerId(),
                        request.getReason(),
                        request.getPhotos()
                )
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<Page<ReturnRequestDTO>> getReturnRequests(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReturnRequestDTO> returnRequests = returnRefundService.getReturnRequests(userId, page, size);
        return ResponseEntity.ok(returnRequests);
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<ReturnRequestDTO>> getAdminReturnRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReturnRequestDTO> returnRequests = returnRefundService.getAllReturnRequests(status, page, size);
        return ResponseEntity.ok(returnRequests);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<SellerReturnStatsDTO> getAdminReturnStats() {
        return ResponseEntity.ok(returnRefundService.getGlobalReturnStats());
    }

    /**
     * Temporary recovery endpoint for backfilling seller IDs on legacy return rows.
     */
    @PostMapping("/admin/backfill-seller-ids")
    public ResponseEntity<Map<String, Long>> backfillMissingSellerIds() {
        return ResponseEntity.ok(returnRefundService.backfillMissingSellerIds());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReturnRequestDTO> approveReturnRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String approvedBy,
            @RequestBody ApproveReturnRequestDTO request) {
        return returnRefundService.approveReturnRequest(id, approvedBy, request.getRefundAmount())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ReturnRequestDTO> rejectReturnRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String rejectedBy,
            @RequestBody RejectReturnRequestDTO request) {
        return returnRefundService.rejectReturnRequest(id, request.getReason(), rejectedBy)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<RefundDTO> processRefund(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Roles", required = false) String userRolesHeader,
            @RequestBody ProcessRefundDTO request) {
        boolean isAdmin = hasRole(userRolesHeader, "ROLE_ADMIN");
        return returnRefundService.processRefund(id, request.getMethod(), request.getDelayMinutes(), isAdmin)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    private boolean hasRole(String userRolesHeader, String expectedRole) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            return false;
        }

        return Arrays.stream(userRolesHeader.split("[,\\s]+"))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .anyMatch(role -> role.equalsIgnoreCase(expectedRole));
    }
}
