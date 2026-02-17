package com.ecommerce.order.services;

import com.ecommerce.order.clients.PaymentGatewayRefundClient;
import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.PaymentGatewayRefundRequest;
import com.ecommerce.order.dtos.PaymentGatewayRefundResponse;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.dtos.ReturnRequestDTO;
import com.ecommerce.order.models.*;
import com.ecommerce.order.repositories.OrderRepository;
import com.ecommerce.order.repositories.RefundRepository;
import com.ecommerce.order.repositories.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnRefundService {
    private static final Set<String> QR_PAYMENT_METHODS = Set.of("QR", "KHQR", "BAKONG", "BAKONG_QR");

    private final ReturnRequestRepository returnRequestRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final PaymentGatewayRefundClient paymentGatewayRefundClient;

    @Transactional
    public Optional<ReturnRequestDTO> createReturnRequest(
            String userId,
            Long orderId,
            String productId,
            String sellerId,
            String reason,
            List<String> photos
    ) {
        // Verify order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("createReturnRequest rejected: order not found (orderId={}, userId={}, productId={})", orderId, userId, productId);
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (!order.getUserId().equals(userId)) {
            log.warn(
                    "createReturnRequest rejected: order ownership mismatch (orderId={}, orderUserId={}, headerUserId={}, productId={})",
                    orderId,
                    order.getUserId(),
                    userId,
                    productId
            );
            return Optional.empty();
        }

        // Only allow returns for delivered orders
        boolean isDelivered = Boolean.TRUE.equals(order.getIsDelivered()) || order.getStatus() == OrderStatus.DELIVERED;
        if (!isDelivered) {
            log.warn(
                    "createReturnRequest rejected: order not delivered (orderId={}, status={}, isDelivered={}, userId={}, productId={})",
                    orderId,
                    order.getStatus(),
                    order.getIsDelivered(),
                    userId,
                    productId
            );
            return Optional.empty();
        }

        // Verify product exists in order
        boolean productExists = order.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        if (!productExists) {
            log.warn(
                    "createReturnRequest rejected: product not in order (orderId={}, userId={}, requestedProductId={}, orderProductIds={})",
                    orderId,
                    userId,
                    productId,
                    order.getItems().stream().map(OrderItem::getProductId).distinct().toList()
            );
            return Optional.empty();
        }

        // Resolve seller ID from authoritative product metadata first, then request fallback.
        String resolvedSellerId = resolveSellerId(productId, sellerId);
        if (resolvedSellerId == null || resolvedSellerId.isBlank()) {
            log.warn(
                    "createReturnRequest rejected: unable to resolve seller (orderId={}, userId={}, productId={}, requestSellerId={})",
                    orderId,
                    userId,
                    productId,
                    sellerId
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to resolve seller for the selected product. Please try again."
            );
        }

        // Create return request
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId(orderId);
        returnRequest.setProductId(productId);
        returnRequest.setUserId(userId);
        returnRequest.setSellerId(resolvedSellerId);
        returnRequest.setReason(reason);
        returnRequest.setPhotos(photos != null ? photos : new java.util.ArrayList<>());
        returnRequest.setStatus(ReturnStatus.PENDING);

        // Calculate refund amount from order item
        BigDecimal refundAmount = order.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .map(item -> {
                    BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    return unitPrice.multiply(BigDecimal.valueOf(quantity));
                })
                .orElse(BigDecimal.ZERO);
        returnRequest.setRefundAmount(refundAmount);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return Optional.of(mapToReturnRequestDTO(saved));
    }

    public Page<ReturnRequestDTO> getAllReturnRequests(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null && !status.isBlank()) {
            try {
                ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
                return returnRequestRepository.findByStatusOrderByCreatedAtDesc(returnStatus, pageable)
                        .map(this::mapToReturnRequestDTO);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter: " + status);
            }
        }

        return returnRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToReturnRequestDTO);
    }

    public SellerReturnStatsDTO getGlobalReturnStats() {
        Long totalReturns = returnRequestRepository.count();
        Long pendingReturns = returnRequestRepository.countByStatus(ReturnStatus.PENDING);
        Long approvedReturns = returnRequestRepository.countByStatus(ReturnStatus.APPROVED);
        Long rejectedReturns = returnRequestRepository.countByStatus(ReturnStatus.REJECTED);
        Long completedReturns = returnRequestRepository.countByStatus(ReturnStatus.COMPLETED);

        Double totalRefundAmount = refundRepository.sumCompletedAmountAll();
        Double averageRefundAmount = totalRefundAmount != null && completedReturns != null && completedReturns > 0
                ? totalRefundAmount / completedReturns
                : 0.0;

        return SellerReturnStatsDTO.builder()
                .totalReturns(totalReturns != null ? totalReturns : 0L)
                .pendingReturns(pendingReturns != null ? pendingReturns : 0L)
                .approvedReturns(approvedReturns != null ? approvedReturns : 0L)
                .rejectedReturns(rejectedReturns != null ? rejectedReturns : 0L)
                .completedReturns(completedReturns != null ? completedReturns : 0L)
                .totalRefundAmount(totalRefundAmount != null ? totalRefundAmount : 0.0)
                .averageRefundAmount(averageRefundAmount)
                .build();
    }

    @Transactional
    public Map<String, Long> backfillMissingSellerIds() {
        List<ReturnRequest> missingSellerReturns = returnRequestRepository.findBySellerIdIsNull();
        long scanned = missingSellerReturns.size();
        long updated = 0L;
        long skipped = 0L;

        for (ReturnRequest returnRequest : missingSellerReturns) {
            String resolvedSellerId = resolveSellerId(returnRequest.getProductId(), null);
            if (resolvedSellerId == null || resolvedSellerId.isBlank()) {
                skipped++;
                continue;
            }

            returnRequest.setSellerId(resolvedSellerId);
            returnRequestRepository.save(returnRequest);
            updated++;
        }

        Map<String, Long> summary = new HashMap<>();
        summary.put("scanned", scanned);
        summary.put("updated", updated);
        summary.put("skipped", skipped);
        return summary;
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
        return processRefund(returnRequestId, method, null, false);
    }

    @Transactional
    public Optional<RefundDTO> processRefund(Long returnRequestId, RefundMethod method, Integer delayMinutes, boolean isAdmin) {
        Optional<ReturnRequest> returnRequestOpt = returnRequestRepository.findById(returnRequestId);
        if (returnRequestOpt.isEmpty()) {
            return Optional.empty();
        }

        ReturnRequest returnRequest = returnRequestOpt.get();
        if (returnRequest.getStatus() != ReturnStatus.APPROVED) {
            return Optional.empty();
        }

        int effectiveDelay = delayMinutes != null ? delayMinutes : 0;
        if (effectiveDelay < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delayMinutes cannot be negative");
        }
        if (effectiveDelay > 0 && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can schedule delayed refunds");
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
        refund.setSellerId(returnRequest.getSellerId());
        refund.setAmount(returnRequest.getRefundAmount());
        refund.setMethod(method != null ? method : RefundMethod.ORIGINAL);
        refund.setStatus(effectiveDelay > 0 ? RefundStatus.SCHEDULED : RefundStatus.PROCESSING);
        if (effectiveDelay > 0) {
            refund.setScheduledAt(LocalDateTime.now().plusMinutes(effectiveDelay));
        }

        Refund saved = refundRepository.save(refund);

        if (saved.getStatus() == RefundStatus.SCHEDULED) {
            return Optional.of(mapToRefundDTO(saved));
        }

        saved = executeRefund(saved, returnRequest);

        return Optional.of(mapToRefundDTO(saved));
    }

    @Scheduled(fixedDelayString = "${refund.scheduler.fixed-delay-ms:60000}")
    @Transactional
    public void processDueScheduledRefunds() {
        List<Refund> dueRefunds = refundRepository.findByStatusAndScheduledAtLessThanEqual(
                RefundStatus.SCHEDULED,
                LocalDateTime.now()
        );

        if (dueRefunds.isEmpty()) {
            return;
        }

        for (Refund refund : dueRefunds) {
            Optional<ReturnRequest> returnRequestOpt = returnRequestRepository.findById(refund.getReturnRequestId());
            if (returnRequestOpt.isEmpty()) {
                markRefundFailed(refund, "Return request not found for scheduled refund");
                continue;
            }

            try {
                executeRefund(refund, returnRequestOpt.get());
            } catch (Exception ex) {
                log.error("Failed to process scheduled refund id={}: {}", refund.getId(), ex.getMessage());
            }
        }
    }

    private Refund executeRefund(Refund refund, ReturnRequest returnRequest) {
        refund.setStatus(RefundStatus.PROCESSING);
        refund.setFailureReason(null);
        refund = refundRepository.save(refund);

        try {
            Order order = orderRepository.findById(returnRequest.getOrderId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Order not found for refund: " + returnRequest.getOrderId()
                    ));

            if (shouldProcessViaPaymentGateway(order, refund.getMethod())) {
                processQrGatewayRefund(order, returnRequest, refund);
            }

            refund.setStatus(RefundStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());
            refund = refundRepository.save(refund);

            returnRequest.setStatus(ReturnStatus.COMPLETED);
            returnRequestRepository.save(returnRequest);

            return refund;
        } catch (ResponseStatusException ex) {
            markRefundFailed(refund, ex.getReason());
            throw ex;
        } catch (Exception ex) {
            markRefundFailed(refund, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to process refund");
        }
    }

    private boolean shouldProcessViaPaymentGateway(Order order, RefundMethod method) {
        if (method != RefundMethod.ORIGINAL) {
            return false;
        }

        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return false;
        }

        return QR_PAYMENT_METHODS.contains(paymentMethod.trim().toUpperCase());
    }

    private void processQrGatewayRefund(Order order, ReturnRequest returnRequest, Refund refund) {
        if (order.getPaymentResult() == null
                || order.getPaymentResult().getPaymentId() == null
                || order.getPaymentResult().getPaymentId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR payment reference is missing. Cannot transfer refund to customer."
            );
        }

        PaymentGatewayRefundRequest gatewayRequest = new PaymentGatewayRefundRequest(
                "REFUND-" + refund.getId(),
                String.valueOf(order.getId()),
                order.getPaymentResult().getPaymentId(),
                refund.getAmount(),
                "USD",
                returnRequest.getReason()
        );

        PaymentGatewayRefundResponse gatewayResponse = paymentGatewayRefundClient.refundQrPayment(gatewayRequest);
        if (gatewayResponse.getStatus() == null || !"COMPLETED".equalsIgnoreCase(gatewayResponse.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway did not confirm refund completion"
            );
        }

        if (gatewayResponse.getProviderRefundId() != null && !gatewayResponse.getProviderRefundId().isBlank()) {
            refund.setGatewayReference(gatewayResponse.getProviderRefundId());
        } else {
            refund.setGatewayReference(gatewayResponse.getRefundId());
        }
    }

    private void markRefundFailed(Refund refund, String reason) {
        refund.setStatus(RefundStatus.FAILED);
        refund.setFailureReason(reason != null ? reason : "Unknown refund processing error");
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);
    }

    private String resolveSellerId(String productId, String fallbackSellerId) {
        try {
            ProductResponse product = productServiceClient.getProductDetails(productId);
            if (product != null && product.getSellerId() != null && !product.getSellerId().isBlank()) {
                return product.getSellerId();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch product details for seller resolution (productId={}): {}", productId, e.getMessage());
        }

        if (fallbackSellerId != null && !fallbackSellerId.isBlank()) {
            return fallbackSellerId;
        }

        return null;
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
                refund.getScheduledAt(),
                refund.getProcessedAt(),
                refund.getGatewayReference(),
                refund.getFailureReason()
        );
    }
}
