package com.ecommerce.order.services;

import com.ecommerce.order.dtos.OrderItemDTO;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.ShippingAddressDTO;
import com.ecommerce.order.dtos.PaymentResultDTO;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;

    // For now, we'll assume all orders are visible to sellers
    // In a real implementation, you'd filter orders based on product seller IDs

    public Page<OrderResponse> getSellerOrders(String sellerId, int page, int size, 
                                                String status, String paymentMethod, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        List<Order> allOrders = orderRepository.findAll(Sort.by("createdAt").descending());
        
        // Filter by status
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                allOrders = allOrders.stream()
                        .filter(o -> o.getStatus() == orderStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        // Filter by payment method
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            allOrders = allOrders.stream()
                    .filter(o -> paymentMethod.equals(o.getPaymentMethod()))
                    .collect(Collectors.toList());
        }
        
        // Filter by search (order ID or customer info)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            allOrders = allOrders.stream()
                    .filter(o -> o.getId().toString().contains(search) ||
                            (o.getUserId() != null && o.getUserId().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }
        
        // Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allOrders.size());
        List<Order> pageOrders = start < allOrders.size() ? allOrders.subList(start, end) : new ArrayList<>();
        
        List<OrderResponse> responses = pageOrders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, allOrders.size());
    }

    public Optional<OrderResponse> getSellerOrderById(String sellerId, Long orderId) {
        return orderRepository.findById(orderId).map(this::mapToOrderResponse);
    }

    public Map<String, Object> getOrderStats(String sellerId) {
        List<Order> allOrders = orderRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allOrders.size());
        stats.put("pending", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        stats.put("confirmed", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count());
        stats.put("shipped", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count());
        stats.put("delivered", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        stats.put("cancelled", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        
        // Revenue stats
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        // Today's stats
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay))
                .count();
        stats.put("todayOrders", todayOrders);
        
        return stats;
    }

    @Transactional
    public Optional<OrderResponse> updateOrderStatus(String sellerId, Long orderId, String status) {
        return orderRepository.findById(orderId).map(order -> {
            try {
                OrderStatus newStatus = OrderStatus.valueOf(status);
                order.setStatus(newStatus);
                
                // Handle specific status updates
                if (newStatus == OrderStatus.DELIVERED) {
                    order.setIsDelivered(true);
                    order.setDeliveredAt(LocalDateTime.now());
                }
                
                return mapToOrderResponse(orderRepository.save(order));
            } catch (IllegalArgumentException e) {
                return mapToOrderResponse(order);
            }
        });
    }

    @Transactional
    public List<OrderResponse> bulkUpdateOrderStatus(String sellerId, List<Long> orderIds, String status) {
        List<OrderResponse> updated = new ArrayList<>();
        for (Long orderId : orderIds) {
            updateOrderStatus(sellerId, orderId, status).ifPresent(updated::add);
        }
        return updated;
    }

    public List<OrderResponse> getPendingOrders(String sellerId) {
        return orderRepository.findByStatus(OrderStatus.PENDING)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersNeedingAttention(String sellerId) {
        return orderRepository.findOrdersNeedingAttention()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<OrderResponse> addPackingNotes(String sellerId, Long orderId, String notes) {
        // In a real implementation, you'd add a packingNotes field to the Order model
        // For now, we just return the order as-is
        return orderRepository.findById(orderId).map(this::mapToOrderResponse);
    }

    public List<OrderResponse> getRecentOrders(String sellerId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findByDateRange(startDate, LocalDateTime.now())
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(item -> new OrderItemDTO(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                        ))
                        .collect(Collectors.toList()),
                order.getShippingAddress() != null
                        ? new ShippingAddressDTO(
                                order.getShippingAddress().getFirstName(),
                                order.getShippingAddress().getLastName(),
                                order.getShippingAddress().getStreet(),
                                order.getShippingAddress().getCity(),
                                order.getShippingAddress().getState(),
                                order.getShippingAddress().getZipCode(),
                                order.getShippingAddress().getCountry(),
                                order.getShippingAddress().getPhone()
                        )
                        : null,
                order.getPaymentMethod(),
                order.getPaymentResult() != null
                        ? new PaymentResultDTO(
                                order.getPaymentResult().getPaymentId(),
                                order.getPaymentResult().getStatus(),
                                order.getPaymentResult().getUpdateTime(),
                                order.getPaymentResult().getEmailAddress()
                        )
                        : null,
                order.getItemsPrice(),
                order.getTaxPrice(),
                order.getShippingPrice(),
                order.getIsPaid(),
                order.getPaidAt(),
                order.getIsDelivered(),
                order.getDeliveredAt(),
                order.getPaypalOrderId(),
                order.getStripeClientSecret(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
