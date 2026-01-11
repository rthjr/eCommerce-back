package com.ecommerce.order.dtos;

import com.ecommerce.order.models.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDTO> items;

    // Shipping Information
    private ShippingAddressDTO shippingAddress;

    // Payment Information
    private String paymentMethod;
    private PaymentResultDTO paymentResult;

    // Price Breakdown
    private BigDecimal itemsPrice;
    private BigDecimal taxPrice;
    private BigDecimal shippingPrice;

    // Payment Status
    private Boolean isPaid;
    private LocalDateTime paidAt;

    // Delivery Status
    private Boolean isDelivered;
    private LocalDateTime deliveredAt;

    // Payment Gateway IDs
    private String paypalOrderId;
    private String stripeClientSecret;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
