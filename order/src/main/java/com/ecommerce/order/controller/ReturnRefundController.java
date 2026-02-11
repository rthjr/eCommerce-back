package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.*;
import com.ecommerce.order.services.ReturnRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody ProcessRefundDTO request) {
        return returnRefundService.processRefund(id, request.getMethod())
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
