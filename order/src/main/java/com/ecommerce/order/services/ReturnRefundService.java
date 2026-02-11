package com.ecommerce.order.services;

import com.ecommerce.order.dtos.CreateReturnRequestDTO;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.dtos.ReturnRequestDTO;
import com.ecommerce.order.models.*;
import com.ecommerce.order.repositories.OrderRepository;
import com.ecommerce.order.repositories.RefundRepository;
import com.ecommerce.order.repositories.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReturnRefundService {
    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Optional<ReturnRequestDTO> createReturnRequest(String userId, Long orderId, String productId, String reason, java.util.List<String> photos) {
        // Verify order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (!order.getUserId().equals(userId)) {
            return Optional.empty();
        }

        // Verify product exists in order
        boolean productExists = order.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        if (!productExists) {
            return Optional.empty();
        }

        // Create return request
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId(orderId);
        returnRequest.setProductId(productId);
        returnRequest.setUserId(userId);
        returnRequest.setReason(reason);
        returnRequest.setPhotos(photos != null ? photos : new java.util.ArrayList<>());
        returnRequest.setStatus(ReturnStatus.PENDING);

        // Calculate refund amount from order item
        BigDecimal refundAmount = order.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .map(OrderItem::getPrice)
                .orElse(BigDecimal.ZERO);
        returnRequest.setRefundAmount(refundAmount);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return Optional.of(mapToReturnRequestDTO(saved));
    }

    public Page<ReturnRequestDTO> getReturnRequests(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToReturnRequestDTO);
    }

    @Transactional
    public Optional<ReturnRequestDTO> approveReturnRequest(Long returnRequestId, String approvedBy, BigDecimal refundAmount) {
        Optional<ReturnRequest> returnRequestOpt = returnRequestRepository.findById(returnRequestId);
        if (returnRequestOpt.isEmpty()) {
            return Optional.empty();
        }

        ReturnRequest returnRequest = returnRequestOpt.get();
        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            return Optional.empty();
        }

        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setApprovedBy(approvedBy);
        returnRequest.setApprovedAt(LocalDateTime.now());
        if (refundAmount != null) {
            returnRequest.setRefundAmount(refundAmount);
        }

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return Optional.of(mapToReturnRequestDTO(saved));
    }

    @Transactional
    public Optional<ReturnRequestDTO> rejectReturnRequest(Long returnRequestId, String rejectionReason, String rejectedBy) {
        Optional<ReturnRequest> returnRequestOpt = returnRequestRepository.findById(returnRequestId);
        if (returnRequestOpt.isEmpty()) {
            return Optional.empty();
        }

        ReturnRequest returnRequest = returnRequestOpt.get();
        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            return Optional.empty();
        }

        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setRejectionReason(rejectionReason);
        returnRequest.setApprovedBy(rejectedBy); // Using approvedBy field for rejectedBy

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return Optional.of(mapToReturnRequestDTO(saved));
    }

    @Transactional
    public Optional<RefundDTO> processRefund(Long returnRequestId, RefundMethod method) {
        Optional<ReturnRequest> returnRequestOpt = returnRequestRepository.findById(returnRequestId);
        if (returnRequestOpt.isEmpty()) {
            return Optional.empty();
        }

        ReturnRequest returnRequest = returnRequestOpt.get();
        if (returnRequest.getStatus() != ReturnStatus.APPROVED) {
            return Optional.empty();
        }

        // Check if refund already exists
        Optional<Refund> existingRefund = refundRepository.findByReturnRequestId(returnRequestId);
        if (existingRefund.isPresent()) {
            return Optional.of(mapToRefundDTO(existingRefund.get()));
        }

        // Create refund
        Refund refund = new Refund();
        refund.setOrderId(returnRequest.getOrderId());
        refund.setReturnRequestId(returnRequestId);
        refund.setAmount(returnRequest.getRefundAmount());
        refund.setMethod(method != null ? method : RefundMethod.ORIGINAL);
        refund.setStatus(RefundStatus.PROCESSING);

        Refund saved = refundRepository.save(refund);

        // Update return request status
        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequestRepository.save(returnRequest);

        // In a real implementation, you would call a payment gateway here
        // For now, we'll mark it as completed immediately
        saved.setStatus(RefundStatus.COMPLETED);
        saved.setProcessedAt(LocalDateTime.now());
        saved = refundRepository.save(saved);

        return Optional.of(mapToRefundDTO(saved));
    }

    private ReturnRequestDTO mapToReturnRequestDTO(ReturnRequest returnRequest) {
        return new ReturnRequestDTO(
                returnRequest.getId(),
                returnRequest.getOrderId(),
                returnRequest.getProductId(),
                returnRequest.getUserId(),
                returnRequest.getReason(),
                returnRequest.getPhotos(),
                returnRequest.getStatus(),
                returnRequest.getRefundAmount(),
                returnRequest.getApprovedBy(),
                returnRequest.getRejectionReason(),
                returnRequest.getCreatedAt(),
                returnRequest.getUpdatedAt(),
                returnRequest.getApprovedAt()
        );
    }

    private RefundDTO mapToRefundDTO(Refund refund) {
        return new RefundDTO(
                refund.getId(),
                refund.getOrderId(),
                refund.getReturnRequestId(),
                refund.getAmount(),
                refund.getMethod(),
                refund.getStatus(),
                refund.getCreatedAt(),
                refund.getProcessedAt()
        );
    }
}
