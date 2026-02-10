package com.ecommerce.product.services;

import com.ecommerce.product.dtos.*;
import com.ecommerce.product.models.InventoryAlert;
import com.ecommerce.product.models.InventoryAlert.AlertType;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.StockMovement;
import com.ecommerce.product.models.StockMovement.MovementType;
import com.ecommerce.product.repositories.InventoryAlertRepository;
import com.ecommerce.product.repositories.ProductRepository;
import com.ecommerce.product.repositories.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryAlertRepository alertRepository;
    private final StockMovementRepository movementRepository;

    // Inventory Alerts
    public List<InventoryAlertResponse> getSellerAlerts(String sellerId) {
        return alertRepository.findBySellerIdAndIsActiveTrue(sellerId)
                .stream()
                .map(this::mapToAlertResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryAlertResponse> getUnreadAlerts(String sellerId) {
        return alertRepository.findBySellerIdAndIsReadFalse(sellerId)
                .stream()
                .map(this::mapToAlertResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadAlertCount(String sellerId) {
        return alertRepository.countBySellerIdAndIsReadFalse(sellerId);
    }

    @Transactional
    public void markAlertAsRead(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setIsRead(true);
            alert.setReadAt(LocalDateTime.now());
            alertRepository.save(alert);
        });
    }

    @Transactional
    public void markAllAlertsAsRead(String sellerId) {
        alertRepository.findBySellerIdAndIsReadFalse(sellerId).forEach(alert -> {
            alert.setIsRead(true);
            alert.setReadAt(LocalDateTime.now());
            alertRepository.save(alert);
        });
    }

    @Transactional
    public void dismissAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setIsActive(false);
            alertRepository.save(alert);
        });
    }

    @Transactional
    public void checkAndGenerateAlerts(Long productId) {
        productRepository.findById(productId).ifPresent(product -> {
            int stock = product.getStockQuantity();
            int threshold = product.getLowStockThreshold() != null ? product.getLowStockThreshold() : 10;

            if (stock == 0) {
                createAlert(product, AlertType.OUT_OF_STOCK, 
                        "Product \"" + product.getName() + "\" is out of stock!");
            } else if (stock <= threshold) {
                createAlert(product, AlertType.LOW_STOCK,
                        "Product \"" + product.getName() + "\" has low stock (" + stock + " units remaining)");
            }
        });
    }

    @Transactional
    public void checkBackInStock(Long productId, int previousStock) {
        if (previousStock == 0) {
            productRepository.findById(productId).ifPresent(product -> {
                if (product.getStockQuantity() > 0) {
                    createAlert(product, AlertType.BACK_IN_STOCK,
                            "Product \"" + product.getName() + "\" is back in stock!");
                }
            });
        }
    }

    private void createAlert(Product product, AlertType type, String message) {
        InventoryAlert alert = new InventoryAlert();
        alert.setProduct(product);
        alert.setSellerId(product.getSellerId());
        alert.setType(type);
        alert.setThreshold(product.getLowStockThreshold());
        alert.setMessage(message);
        alert.setIsActive(true);
        alert.setIsRead(false);
        alertRepository.save(alert);
    }

    // Stock Movement
    @Transactional
    public StockMovementResponse addStock(String sellerId, Long productId, StockAdjustmentRequest request) {
        return adjustStock(sellerId, productId, Math.abs(request.getQuantity()), 
                MovementType.ADD, request.getReason(), request.getPerformedBy());
    }

    @Transactional
    public StockMovementResponse removeStock(String sellerId, Long productId, StockAdjustmentRequest request) {
        return adjustStock(sellerId, productId, -Math.abs(request.getQuantity()), 
                MovementType.REMOVE, request.getReason(), request.getPerformedBy());
    }

    @Transactional
    public StockMovementResponse adjustStock(String sellerId, Long productId, Integer quantity,
                                              MovementType type, String reason, String performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Not authorized to modify this product");
        }

        int previousStock = product.getStockQuantity();
        int newStock = previousStock + quantity;
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setSellerId(sellerId);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setType(type);
        movement.setReason(reason);
        movement.setPerformedBy(performedBy);
        
        StockMovement saved = movementRepository.save(movement);

        // Check for alerts
        checkAndGenerateAlerts(productId);
        checkBackInStock(productId, previousStock);

        return mapToMovementResponse(saved);
    }

    @Transactional
    public StockMovementResponse recordSale(Long productId, Integer quantity, Long orderId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        int previousStock = product.getStockQuantity();
        int newStock = previousStock - quantity;

        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setSellerId(product.getSellerId());
        movement.setQuantity(-quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setType(MovementType.SALE);
        movement.setReason("Order #" + orderId);
        movement.setOrderId(orderId);

        StockMovement saved = movementRepository.save(movement);

        // Check for low stock alerts
        checkAndGenerateAlerts(productId);

        return mapToMovementResponse(saved);
    }

    public List<StockMovementResponse> getProductStockHistory(Long productId) {
        return movementRepository.findByProductId(productId)
                .stream()
                .map(this::mapToMovementResponse)
                .collect(Collectors.toList());
    }

    public Page<StockMovementResponse> getProductStockHistoryPaged(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return movementRepository.findByProductId(productId, pageable).map(this::mapToMovementResponse);
    }

    public Page<StockMovementResponse> getSellerStockMovements(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return movementRepository.findBySellerId(sellerId, pageable).map(this::mapToMovementResponse);
    }

    // Inventory Overview
    public InventoryOverviewResponse getInventoryOverview(String sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Product::getStockQuantity).sum();
        int outOfStock = (int) products.stream().filter(p -> p.getStockQuantity() == 0).count();
        int lowStock = (int) products.stream()
                .filter(p -> {
                    int stock = p.getStockQuantity();
                    int threshold = p.getLowStockThreshold() != null ? p.getLowStockThreshold() : 10;
                    return stock > 0 && stock <= threshold;
                }).count();
        int healthyStock = totalProducts - outOfStock - lowStock;
        
        long unreadAlerts = alertRepository.countBySellerIdAndIsReadFalse(sellerId);

        return new InventoryOverviewResponse(
                totalProducts, totalStock, outOfStock, lowStock, 
                healthyStock, unreadAlerts
        );
    }

    // Low stock products
    public List<LowStockProductResponse> getLowStockProducts(String sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .filter(p -> {
                    int stock = p.getStockQuantity();
                    int threshold = p.getLowStockThreshold() != null ? p.getLowStockThreshold() : 10;
                    return stock <= threshold;
                })
                .map(this::mapToLowStockResponse)
                .collect(Collectors.toList());
    }

    // Update inventory settings
    @Transactional
    public Optional<Product> updateInventorySettings(Long productId, InventorySettingsRequest request) {
        return productRepository.findById(productId).map(product -> {
            if (request.getLowStockThreshold() != null) {
                product.setLowStockThreshold(request.getLowStockThreshold());
            }
            if (request.getReorderPoint() != null) {
                product.setReorderPoint(request.getReorderPoint());
            }
            if (request.getReorderQuantity() != null) {
                product.setReorderQuantity(request.getReorderQuantity());
            }
            if (request.getSku() != null) {
                product.setSku(request.getSku());
            }
            return productRepository.save(product);
        });
    }

    private InventoryAlertResponse mapToAlertResponse(InventoryAlert alert) {
        InventoryAlertResponse response = new InventoryAlertResponse();
        response.setId(alert.getId());
        response.setProductId(alert.getProduct().getId());
        response.setProductName(alert.getProduct().getName());
        response.setProductSku(alert.getProduct().getSku());
        response.setCurrentStock(alert.getProduct().getStockQuantity());
        response.setType(alert.getType());
        response.setThreshold(alert.getThreshold());
        response.setMessage(alert.getMessage());
        response.setIsRead(alert.getIsRead());
        response.setCreatedAt(alert.getCreatedAt());
        response.setReadAt(alert.getReadAt());
        return response;
    }

    private StockMovementResponse mapToMovementResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setProductId(movement.getProduct().getId());
        response.setProductName(movement.getProduct().getName());
        response.setQuantity(movement.getQuantity());
        response.setPreviousStock(movement.getPreviousStock());
        response.setNewStock(movement.getNewStock());
        response.setType(movement.getType());
        response.setReason(movement.getReason());
        response.setPerformedBy(movement.getPerformedBy());
        response.setOrderId(movement.getOrderId());
        response.setCreatedAt(movement.getCreatedAt());
        return response;
    }

    private LowStockProductResponse mapToLowStockResponse(Product product) {
        LowStockProductResponse response = new LowStockProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setCurrentStock(product.getStockQuantity());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setReorderPoint(product.getReorderPoint());
        response.setReorderQuantity(product.getReorderQuantity());
        response.setImageUrl(product.getImageUrl());
        return response;
    }
}
