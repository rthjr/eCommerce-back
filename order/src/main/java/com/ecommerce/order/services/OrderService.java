package com.ecommerce.order.services;

import com.ecommerce.order.repositories.OrderRepository;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.dtos.OrderItemDTO;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
        private final CartService cartService;
        private final OrderRepository orderRepository;

        public Optional<OrderResponse> createOrder(String userId) {
                // Validate for cart items
                List<CartItem> cartItems = cartService.getCart(userId);
                if (cartItems.isEmpty()) {
                        return Optional.empty();
                }
                // // Validate for user
                //
                // Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
                // if (userOptional.isEmpty()) {
                // return Optional.empty();
                // }
                // User user = userOptional.get();

                // Calculate total price
                BigDecimal totalPrice = cartItems.stream()
                                .map(CartItem::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Create order
                Order order = new Order();
                order.setUserId(userId);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setTotalAmount(totalPrice);

                List<OrderItem> orderItems = cartItems.stream()
                                .map(item -> new OrderItem(
                                                null,
                                                item.getProductId(),
                                                item.getQuantity(),
                                                item.getPrice(),
                                                order))
                                .collect(java.util.stream.Collectors.toList());

                order.setItems(orderItems);
                Order savedOrder = orderRepository.save(order);

                // Clear the cart
                cartService.clearCart(userId);

                return Optional.of(mapToOrderResponse(savedOrder));
        }

        private OrderResponse mapToOrderResponse(Order order) {
                return new OrderResponse(
                                order.getId(),
                                order.getUserId(),
                                order.getTotalAmount(),
                                order.getStatus(),
                                order.getItems().stream()
                                                .map(orderItem -> new OrderItemDTO(
                                                                orderItem.getId(),
                                                                orderItem.getProductId(),
                                                                orderItem.getQuantity(),
                                                                orderItem.getPrice(),
                                                                orderItem.getPrice()
                                                                                .multiply(new BigDecimal(orderItem
                                                                                                .getQuantity()))))
                                                .toList(),
                                order.getShippingAddress() != null ? mapToShippingAddressDTO(order.getShippingAddress())
                                                : null,
                                order.getPaymentMethod(),
                                order.getPaymentResult() != null ? mapToPaymentResultDTO(order.getPaymentResult())
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
                                order.getUpdatedAt());
        }

        private com.ecommerce.order.dtos.ShippingAddressDTO mapToShippingAddressDTO(
                        com.ecommerce.order.models.ShippingAddress address) {
                return new com.ecommerce.order.dtos.ShippingAddressDTO(
                                address.getFirstName(),
                                address.getLastName(),
                                address.getStreet(),
                                address.getCity(),
                                address.getState(),
                                address.getZipCode(),
                                address.getCountry(),
                                address.getPhone());
        }

        private com.ecommerce.order.dtos.PaymentResultDTO mapToPaymentResultDTO(
                        com.ecommerce.order.models.PaymentResult payment) {
                return new com.ecommerce.order.dtos.PaymentResultDTO(
                                payment.getPaymentId(),
                                payment.getStatus(),
                                payment.getUpdateTime(),
                                payment.getEmailAddress());
        }

        // Get order by ID
        public Optional<OrderResponse> getOrderById(Long id) {
                return orderRepository.findById(id)
                                .map(this::mapToOrderResponse);
        }

        // Get all orders (admin)
        public List<OrderResponse> getAllOrders() {
                return orderRepository.findAll().stream()
                                .map(this::mapToOrderResponse)
                                .toList();
        }

        // Get user's orders
        public List<OrderResponse> getUserOrders(String userId) {
                return orderRepository.findByUserId(userId).stream()
                                .map(this::mapToOrderResponse)
                                .toList();
        }

        // Mark order as paid
        public Optional<OrderResponse> markAsPaid(Long id, com.ecommerce.order.dtos.PaymentResultDTO paymentResult) {
                return orderRepository.findById(id)
                                .map(order -> {
                                        order.setIsPaid(true);
                                        order.setPaidAt(java.time.LocalDateTime.now());
                                        if (paymentResult != null) {
                                                com.ecommerce.order.models.PaymentResult payment = new com.ecommerce.order.models.PaymentResult();
                                                payment.setPaymentId(paymentResult.getPaymentId());
                                                payment.setStatus(paymentResult.getStatus());
                                                payment.setUpdateTime(paymentResult.getUpdateTime());
                                                payment.setEmailAddress(paymentResult.getEmailAddress());
                                                order.setPaymentResult(payment);
                                        }
                                        return orderRepository.save(order);
                                })
                                .map(this::mapToOrderResponse);
        }

        // Mark order as delivered (admin)
        public Optional<OrderResponse> markAsDelivered(Long id) {
                return orderRepository.findById(id)
                                .map(order -> {
                                        order.setIsDelivered(true);
                                        order.setDeliveredAt(java.time.LocalDateTime.now());
                                        return orderRepository.save(order);
                                })
                                .map(this::mapToOrderResponse);
        }
}
