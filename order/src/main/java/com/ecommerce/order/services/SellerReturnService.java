package com.ecommerce.order.services;

import com.ecommerce.order.dto.ApproveReturnRequestDTO;
import com.ecommerce.order.dto.RejectReturnRequestDTO;
import com.ecommerce.order.dto.ReturnRequestDTO;
import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.models.ReturnRequest;
import com.ecommerce.order.models.RefundMethod;
import com.ecommerce.order.models.ReturnStatus;
import com.ecommerce.order.repositories.RefundRepository;
import com.ecommerce.order.repositories.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for seller-scoped return management
 * Allows sellers to manage returns for products they sell
 */
@Service
@RequiredArgsConstructor
public class SellerReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final ReturnRefundService returnRefundService;

    /**
     * Get return requests for seller's products
     *
     * @param sellerId The seller's user ID
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Paginated list of return requests
     */
    public Page<ReturnRequestDTO> getSellerReturnRequests(String sellerId, String status, Pageable pageable) {
        Page<ReturnRequest> returns;

        if (status != null && !status.isEmpty()) {
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            returns = returnRequestRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, returnStatus, pageable);
        } else {
            returns = returnRequestRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        }

        return returns.map(this::convertToDTO);
    }

    /**
     * Get a specific return request by ID (seller-scoped)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @return Return request details
     * @throws RuntimeException if return not found or not owned by seller
     */
    public ReturnRequestDTO getSellerReturnRequestById(String sellerId, Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        // Verify that this return belongs to the seller's product
        if (!sellerId.equals(returnRequest.getSellerId())) {
            throw new RuntimeException("Unauthorized: This return request is not for your product");
        }

        return convertToDTO(returnRequest);
    }

    /**
     * Approve a return request (seller can approve for their own products)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @param approveDTO Approval details
     * @return Updated return request
     * @throws RuntimeException if return not found or unauthorized
     */
    @Transactional
    public ReturnRequestDTO approveReturnRequest(String sellerId, Long returnId, ApproveReturnRequestDTO approveDTO) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        // Verify that this return belongs to the seller's product
        if (!sellerId.equals(returnRequest.getSellerId())) {
            throw new RuntimeException("Unauthorized: You cannot approve returns for products you don't sell");
        }

        // Verify current status is PENDING
        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            throw new RuntimeException("Return request cannot be approved in current status: " + returnRequest.getStatus());
        }

        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setRefundAmount(approveDTO.getRefundAmount());
        returnRequest.setApprovedBy(sellerId);
        returnRequest.setApprovedAt(LocalDateTime.now());

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return convertToDTO(saved);
    }

    /**
     * Reject a return request (seller can reject for their own products)
     *
     * @param sellerId The seller's user ID
     * @param returnId The return request ID
     * @param rejectDTO Rejection details
     * @return Updated return request
     * @throws RuntimeException if return not found or unauthorized
     */
    @Transactional
    public ReturnRequestDTO rejectReturnRequest(String sellerId, Long returnId, RejectReturnRequestDTO rejectDTO) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        // Verify that this return belongs to the seller's product
        if (!sellerId.equals(returnRequest.getSellerId())) {
            throw new RuntimeException("Unauthorized: You cannot reject returns for products you don't sell");
        }

        // Verify current status is PENDING
        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            throw new RuntimeException("Return request cannot be rejected in current status: " + returnRequest.getStatus());
        }

        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setRejectionReason(rejectDTO.getReason());
        returnRequest.setApprovedBy(sellerId);
        returnRequest.setApprovedAt(LocalDateTime.now());

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return convertToDTO(saved);
    }

    /**
     * Get return statistics for seller's products
     *
     * @param sellerId The seller's user ID
     * @return Return statistics
     */
    public SellerReturnStatsDTO getSellerReturnStats(String sellerId) {
        Long totalReturns = returnRequestRepository.countBySellerId(sellerId);
        Long pendingReturns = returnRequestRepository.countBySellerIdAndStatus(sellerId, ReturnStatus.PENDING);
        Long approvedReturns = returnRequestRepository.countBySellerIdAndStatus(sellerId, ReturnStatus.APPROVED);
        Long rejectedReturns = returnRequestRepository.countBySellerIdAndStatus(sellerId, ReturnStatus.REJECTED);
        Long completedReturns = returnRequestRepository.countBySellerIdAndStatus(sellerId, ReturnStatus.COMPLETED);

        // Calculate total and average refund amount
        Double totalRefundAmount = refundRepository.sumAmountBySellerId(sellerId);
        Double averageRefundAmount = totalRefundAmount != null && completedReturns > 0
                ? totalRefundAmount / completedReturns
                : 0.0;

        return SellerReturnStatsDTO.builder()
                .totalReturns(totalReturns)
                .pendingReturns(pendingReturns)
                .approvedReturns(approvedReturns)
                .rejectedReturns(rejectedReturns)
                .completedReturns(completedReturns)
                .totalRefundAmount(totalRefundAmount != null ? totalRefundAmount : 0.0)
                .averageRefundAmount(averageRefundAmount)
                .build();
    }

    /**
     * Process a refund for an approved return request (seller-scoped)
     */
    @Transactional
    public RefundDTO processRefund(String sellerId, Long returnId, RefundMethod method, Integer delayMinutes) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));

        if (!sellerId.equals(returnRequest.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: This return request is not for your product");
        }

        if (returnRequest.getStatus() != ReturnStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Return request cannot be refunded in current status: " + returnRequest.getStatus());
        }

        return returnRefundService.processRefund(returnId, method, delayMinutes, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to process refund"));
    }

    /**
     * Convert ReturnRequest entity to DTO
     */
    private ReturnRequestDTO convertToDTO(ReturnRequest returnRequest) {
        return ReturnRequestDTO.builder()
                .id(returnRequest.getId())
                .orderId(returnRequest.getOrderId())
                .productId(returnRequest.getProductId())
                .userId(returnRequest.getUserId())
                .sellerId(returnRequest.getSellerId())
                .reason(returnRequest.getReason())
                .photos(returnRequest.getPhotos())
                .status(returnRequest.getStatus())
                .refundAmount(returnRequest.getRefundAmount())
                .approvedBy(returnRequest.getApprovedBy())
                .rejectionReason(returnRequest.getRejectionReason())
                .createdAt(returnRequest.getCreatedAt())
                .updatedAt(returnRequest.getUpdatedAt())
                .approvedAt(returnRequest.getApprovedAt())
                .build();
    }
}
