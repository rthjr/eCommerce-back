package com.ecommerce.order.controllers;

import com.ecommerce.order.dto.ApproveReturnRequestDTO;
import com.ecommerce.order.dto.RejectReturnRequestDTO;
import com.ecommerce.order.dto.ReturnRequestDTO;
import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.ProcessRefundDTO;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.services.SellerReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for seller-scoped return management
 * Allows sellers to manage returns for products they sell
 */
@RestController
@RequestMapping("/api/my-products/returns")
@RequiredArgsConstructor
public class SellerReturnController {

    private final SellerReturnService sellerReturnService;

    /**
     * Get all return requests for seller's products
     *
     * @param sellerId The seller's user ID from X-User-ID header
     * @param status Optional status filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of return requests
     */
    @GetMapping
    public ResponseEntity<Page<ReturnRequestDTO>> getSellerReturnRequests(
            @RequestHeader("X-User-ID") String sellerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReturnRequestDTO> returns = sellerReturnService.getSellerReturnRequests(sellerId, status, pageable);
        return ResponseEntity.ok(returns);
    }

    /**
     * Get a specific return request by ID (seller-scoped)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @return Return request details
     */
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnRequestDTO> getSellerReturnRequestById(
            @RequestHeader("X-User-ID") String sellerId,
            @PathVariable Long returnId) {

        ReturnRequestDTO returnRequest = sellerReturnService.getSellerReturnRequestById(sellerId, returnId);
        return ResponseEntity.ok(returnRequest);
    }

    /**
     * Approve a return request (seller can approve for their own products)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @param approveDTO Approval details with refund amount
     * @return Updated return request
     */
    @PutMapping("/{returnId}/approve")
    public ResponseEntity<ReturnRequestDTO> approveReturnRequest(
            @RequestHeader("X-User-ID") String sellerId,
            @PathVariable Long returnId,
            @RequestBody ApproveReturnRequestDTO approveDTO) {

        ReturnRequestDTO updatedReturn = sellerReturnService.approveReturnRequest(sellerId, returnId, approveDTO);
        return ResponseEntity.ok(updatedReturn);
    }

    /**
     * Reject a return request (seller can reject for their own products)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @param rejectDTO Rejection details with reason
     * @return Updated return request
     */
    @PutMapping("/{returnId}/reject")
    public ResponseEntity<ReturnRequestDTO> rejectReturnRequest(
            @RequestHeader("X-User-ID") String sellerId,
            @PathVariable Long returnId,
            @RequestBody RejectReturnRequestDTO rejectDTO) {

        ReturnRequestDTO updatedReturn = sellerReturnService.rejectReturnRequest(sellerId, returnId, rejectDTO);
        return ResponseEntity.ok(updatedReturn);
    }

    /**
     * Process refund for an approved return request (seller-scoped)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @param request Refund processing details
     * @return Refund details
     */
    @PostMapping("/{returnId}/refund")
    public ResponseEntity<RefundDTO> processRefund(
            @RequestHeader("X-User-ID") String sellerId,
            @PathVariable Long returnId,
            @RequestBody ProcessRefundDTO request) {

        RefundDTO refund = sellerReturnService.processRefund(sellerId, returnId, request.getMethod());
        return new ResponseEntity<>(refund, HttpStatus.CREATED);
    }

    /**
     * Get return statistics for seller's products
     *
     * @param sellerId The seller's user ID
     * @return Return statistics (total, pending, approved, rejected, completed)
     */
    @GetMapping("/stats")
    public ResponseEntity<SellerReturnStatsDTO> getSellerReturnStats(
            @RequestHeader("X-User-ID") String sellerId) {

        SellerReturnStatsDTO stats = sellerReturnService.getSellerReturnStats(sellerId);
        return ResponseEntity.ok(stats);
    }
}
