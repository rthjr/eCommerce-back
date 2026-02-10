package com.ecommerce.product.controllers;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    // Overview
    @GetMapping("/overview")
    public ResponseEntity<InventoryOverviewResponse> getInventoryOverview(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(inventoryService.getInventoryOverview(sellerId));
    }

    // Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<InventoryAlertResponse>> getAlerts(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(inventoryService.getSellerAlerts(sellerId));
    }

    @GetMapping("/alerts/unread")
    public ResponseEntity<List<InventoryAlertResponse>> getUnreadAlerts(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(inventoryService.getUnreadAlerts(sellerId));
    }

    @GetMapping("/alerts/count")
    public ResponseEntity<Long> getUnreadAlertCount(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(inventoryService.getUnreadAlertCount(sellerId));
    }

    @PutMapping("/alerts/{id}/read")
    public ResponseEntity<Void> markAlertAsRead(@PathVariable Long id) {
        inventoryService.markAlertAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/alerts/read-all")
    public ResponseEntity<Void> markAllAlertsAsRead(
            @RequestHeader("X-User-Id") String sellerId) {
        inventoryService.markAllAlertsAsRead(sellerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/alerts/{id}/dismiss")
    public ResponseEntity<Void> dismissAlert(@PathVariable Long id) {
        inventoryService.dismissAlert(id);
        return ResponseEntity.ok().build();
    }

    // Stock adjustments
    @PostMapping("/products/{productId}/add-stock")
    public ResponseEntity<StockMovementResponse> addStock(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long productId,
            @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.addStock(sellerId, productId, request));
    }

    @PostMapping("/products/{productId}/remove-stock")
    public ResponseEntity<StockMovementResponse> removeStock(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable Long productId,
            @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.removeStock(sellerId, productId, request));
    }

    // Stock history
    @GetMapping("/products/{productId}/history")
    public ResponseEntity<List<StockMovementResponse>> getProductStockHistory(
            @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockHistory(productId));
    }

    @GetMapping("/products/{productId}/history/paged")
    public ResponseEntity<Page<StockMovementResponse>> getProductStockHistoryPaged(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inventoryService.getProductStockHistoryPaged(productId, page, size));
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<StockMovementResponse>> getSellerStockMovements(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.getSellerStockMovements(sellerId, page, size));
    }

    // Low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockProductResponse>> getLowStockProducts(
            @RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(inventoryService.getLowStockProducts(sellerId));
    }

    // Inventory settings
    @PutMapping("/products/{productId}/settings")
    public ResponseEntity<Void> updateInventorySettings(
            @PathVariable Long productId,
            @RequestBody InventorySettingsRequest request) {
        return inventoryService.updateInventorySettings(productId, request)
                .map(p -> ResponseEntity.ok().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }
}
