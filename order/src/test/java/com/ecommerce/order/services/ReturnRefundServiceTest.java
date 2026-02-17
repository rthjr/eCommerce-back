package com.ecommerce.order.services;

import com.ecommerce.order.clients.PaymentGatewayRefundClient;
import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dto.SellerReturnStatsDTO;
import com.ecommerce.order.dtos.PaymentGatewayRefundResponse;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.RefundDTO;
import com.ecommerce.order.dtos.ReturnRequestDTO;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.models.PaymentResult;
import com.ecommerce.order.models.Refund;
import com.ecommerce.order.models.RefundMethod;
import com.ecommerce.order.models.RefundStatus;
import com.ecommerce.order.models.ReturnRequest;
import com.ecommerce.order.models.ReturnStatus;
import com.ecommerce.order.repositories.OrderRepository;
import com.ecommerce.order.repositories.RefundRepository;
import com.ecommerce.order.repositories.ReturnRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnRefundServiceTest {

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private PaymentGatewayRefundClient paymentGatewayRefundClient;

    @InjectMocks
    private ReturnRefundService returnRefundService;

    @Test
    void createReturnRequestStoresResolvedSellerWhenAvailable() {
        Order order = buildDeliveredOrder("demo-customer-001", "1001", 2, new BigDecimal("10.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ProductResponse product = new ProductResponse();
        product.setSellerId("demo-seller-001");
        when(productServiceClient.getProductDetails("1001")).thenReturn(product);
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> {
            ReturnRequest saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Optional<ReturnRequestDTO> result = returnRefundService.createReturnRequest(
                "demo-customer-001",
                1L,
                "1001",
                null,
                "Damaged item",
                List.of("https://example.com/photo.jpg")
        );

        assertTrue(result.isPresent());
        assertEquals(99L, result.get().getId());
        assertEquals(new BigDecimal("20.00"), result.get().getRefundAmount());

        ArgumentCaptor<ReturnRequest> requestCaptor = ArgumentCaptor.forClass(ReturnRequest.class);
        verify(returnRequestRepository).save(requestCaptor.capture());
        assertEquals("demo-seller-001", requestCaptor.getValue().getSellerId());
    }

    @Test
    void createReturnRequestThrowsBadRequestWhenSellerCannotBeResolved() {
        Order order = buildDeliveredOrder("demo-customer-001", "1001", 1, new BigDecimal("10.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productServiceClient.getProductDetails("1001")).thenThrow(new RuntimeException("product service down"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                returnRefundService.createReturnRequest(
                        "demo-customer-001",
                        1L,
                        "1001",
                        null,
                        "Damaged item",
                        List.of("https://example.com/photo.jpg")
                )
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(returnRequestRepository, never()).save(any(ReturnRequest.class));
    }

    @Test
    void getAllReturnRequestsReturnsAllRowsForAdminView() {
        ReturnRequest request = new ReturnRequest();
        request.setId(1L);
        request.setOrderId(10L);
        request.setProductId("1001");
        request.setUserId("demo-customer-001");
        request.setReason("Damaged");
        request.setPhotos(List.of());
        request.setStatus(ReturnStatus.PENDING);

        when(returnRequestRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(request)));

        Page<ReturnRequestDTO> page = returnRefundService.getAllReturnRequests(null, 0, 10);

        assertEquals(1, page.getContent().size());
        verify(returnRequestRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    @Test
    void getGlobalReturnStatsAggregatesCountsAndRefundAmount() {
        when(returnRequestRepository.count()).thenReturn(8L);
        when(returnRequestRepository.countByStatus(ReturnStatus.PENDING)).thenReturn(2L);
        when(returnRequestRepository.countByStatus(ReturnStatus.APPROVED)).thenReturn(3L);
        when(returnRequestRepository.countByStatus(ReturnStatus.REJECTED)).thenReturn(1L);
        when(returnRequestRepository.countByStatus(ReturnStatus.COMPLETED)).thenReturn(2L);
        when(refundRepository.sumCompletedAmountAll()).thenReturn(100.0);

        SellerReturnStatsDTO stats = returnRefundService.getGlobalReturnStats();

        assertEquals(8L, stats.getTotalReturns());
        assertEquals(2L, stats.getPendingReturns());
        assertEquals(2L, stats.getCompletedReturns());
        assertEquals(100.0, stats.getTotalRefundAmount());
        assertEquals(50.0, stats.getAverageRefundAmount());
    }

    @Test
    void backfillMissingSellerIdsUpdatesOnlyResolvableRows() {
        ReturnRequest resolvable = new ReturnRequest();
        resolvable.setId(1L);
        resolvable.setProductId("1001");

        ReturnRequest unresolvable = new ReturnRequest();
        unresolvable.setId(2L);
        unresolvable.setProductId("1002");

        when(returnRequestRepository.findBySellerIdIsNull()).thenReturn(List.of(resolvable, unresolvable));

        ProductResponse product = new ProductResponse();
        product.setSellerId("demo-seller-001");
        when(productServiceClient.getProductDetails("1001")).thenReturn(product);
        when(productServiceClient.getProductDetails("1002")).thenThrow(new RuntimeException("not found"));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Long> summary = returnRefundService.backfillMissingSellerIds();

        assertEquals(2L, summary.get("scanned"));
        assertEquals(1L, summary.get("updated"));
        assertEquals(1L, summary.get("skipped"));
        verify(returnRequestRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void processRefundTransfersQrPaymentThroughGateway() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(5L);
        returnRequest.setOrderId(10L);
        returnRequest.setSellerId("demo-seller-001");
        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setReason("Damaged");
        returnRequest.setRefundAmount(new BigDecimal("25.00"));

        Order order = new Order();
        order.setId(10L);
        order.setPaymentMethod("QR");
        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setPaymentId("ORD-QR-001");
        paymentResult.setStatus("PAID");
        order.setPaymentResult(paymentResult);

        when(returnRequestRepository.findById(5L)).thenReturn(Optional.of(returnRequest));
        when(refundRepository.findByReturnRequestId(5L)).thenReturn(Optional.empty());
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            if (refund.getId() == null) {
                refund.setId(99L);
            }
            return refund;
        });
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGatewayRefundClient.refundQrPayment(any())).thenReturn(
                new PaymentGatewayRefundResponse("REFUND-99", "COMPLETED", "BKRF-123", LocalDateTime.now().toString(), "ok")
        );

        Optional<RefundDTO> result = returnRefundService.processRefund(5L, RefundMethod.ORIGINAL, null, false);

        assertTrue(result.isPresent());
        assertEquals(RefundStatus.COMPLETED, result.get().getStatus());
        assertEquals("BKRF-123", result.get().getGatewayReference());
        verify(paymentGatewayRefundClient, times(1)).refundQrPayment(any());
        verify(returnRequestRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void processRefundSchedulesWhenAdminProvidesDelay() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(7L);
        returnRequest.setOrderId(11L);
        returnRequest.setSellerId("demo-seller-001");
        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setRefundAmount(new BigDecimal("30.00"));

        when(returnRequestRepository.findById(7L)).thenReturn(Optional.of(returnRequest));
        when(refundRepository.findByReturnRequestId(7L)).thenReturn(Optional.empty());
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            if (refund.getId() == null) {
                refund.setId(101L);
            }
            return refund;
        });

        Optional<RefundDTO> result = returnRefundService.processRefund(7L, RefundMethod.ORIGINAL, 60, true);

        assertTrue(result.isPresent());
        assertEquals(RefundStatus.SCHEDULED, result.get().getStatus());
        assertNotNull(result.get().getScheduledAt());
        verify(orderRepository, never()).findById(any());
        verify(paymentGatewayRefundClient, never()).refundQrPayment(any());
        verify(returnRequestRepository, never()).save(any(ReturnRequest.class));
    }

    @Test
    void processRefundRejectsDelayedRefundForNonAdmin() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(8L);
        returnRequest.setStatus(ReturnStatus.APPROVED);

        when(returnRequestRepository.findById(8L)).thenReturn(Optional.of(returnRequest));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> returnRefundService.processRefund(8L, RefundMethod.ORIGINAL, 10, false)
        );

        assertEquals(403, ex.getStatusCode().value());
        verify(refundRepository, never()).save(any(Refund.class));
        verify(paymentGatewayRefundClient, never()).refundQrPayment(any());
    }

    private Order buildDeliveredOrder(String userId, String productId, int qty, BigDecimal price) {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(userId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setIsDelivered(true);

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(qty);
        item.setPrice(price);
        item.setOrder(order);

        order.setItems(List.of(item));
        return order;
    }
}
