package com.ecommerce.order.controller;

import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.services.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    // Get all seller orders with filtering
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getSellerOrders(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(sellerOrderService.getSellerOrders(sellerId, page, size, status, paymentMethod, search));
    }

    // Get order by ID (only if seller owns any product in the order)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long id) {
        return sellerOrderService.getSellerOrderById(sellerId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get order statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(sellerOrderService.getOrderStats(sellerId));
    }

    // Update order status
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return sellerOrderService.updateOrderStatus(sellerId, id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Bulk update order status
    @PutMapping("/bulk-status")
    public ResponseEntity<List<OrderResponse>> bulkUpdateOrderStatus(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> orderIds = (List<Long>) body.get("orderIds");
        String status = (String) body.get("status");
        return ResponseEntity.ok(sellerOrderService.bulkUpdateOrderStatus(sellerId, orderIds, status));
    }

    // Get pending orders
    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(sellerOrderService.getPendingOrders(sellerId));
    }

    // Get orders needing attention (pending, failed payment, etc.)
    @GetMapping("/attention")
    public ResponseEntity<List<OrderResponse>> getOrdersNeedingAttention(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(sellerOrderService.getOrdersNeedingAttention(sellerId));
    }

    // Add packing notes
    @PutMapping("/{id}/notes")
    public ResponseEntity<OrderResponse> addPackingNotes(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String notes = body.get("notes");
        return sellerOrderService.addPackingNotes(sellerId, id, notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get recent orders (last 7 days)
    @GetMapping("/recent")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(sellerOrderService.getRecentOrders(sellerId, days));
    }
}
