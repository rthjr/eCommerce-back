package com.ecommerce.order.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dtos.CreateOrderRequest;
import com.ecommerce.order.dtos.OrderItemDTO;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.ShippingAddressDTO;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.models.ShippingAddress;
import com.ecommerce.order.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final CartService cartService;
	private final OrderRepository orderRepository;
	private final ProductServiceClient productServiceClient;
	private final ShippingPricingService shippingPricingService;

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	public Optional<OrderResponse> createOrder(String userId, CreateOrderRequest request) {
		List<CartItem> cartItems = cartService.getCart(userId);
		if (cartItems.isEmpty()) {
			return Optional.empty();
		}

		// VALIDATE AND REDUCE STOCK FOR EACH ITEM
		for (CartItem item : cartItems) {
			try {
				// Check if product exists and has enough stock
				ProductResponse product = productServiceClient.getProductDetails(item.getProductId());
				if (product == null || product.getStockQuantity() < item.getQuantity()) {
					throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
				}

				// Reduce stock
				productServiceClient.reduceStock(item.getProductId(), item.getQuantity());
			} catch (Exception e) {
				throw new RuntimeException("Failed to process order: " + e.getMessage());
			}
		}

		// Continue with existing order creation logic...
		BigDecimal itemsPrice = cartItems.stream()
				.map(item -> safeMoney(item.getPrice())
						.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		itemsPrice = money(itemsPrice);
		BigDecimal shippingPrice = Optional.ofNullable(request).map(CreateOrderRequest::getShippingAddress)
				.map(shippingPricingService::quoteShipping)
				.map(com.ecommerce.order.dtos.ShippingQuoteResponse::getShippingPrice).map(this::money)
				.orElseGet(() -> money(BigDecimal.ZERO));
		BigDecimal taxPrice = money(BigDecimal.ZERO);
		BigDecimal totalPrice = money(itemsPrice.add(shippingPrice).add(taxPrice));

		Order order = new Order();
		order.setUserId(userId);
		order.setStatus(OrderStatus.CONFIRMED);
		order.setItemsPrice(itemsPrice);
		order.setShippingPrice(shippingPrice);
		order.setTaxPrice(taxPrice);
		order.setTotalAmount(totalPrice);
		if (request != null) {
			if (request.getShippingAddress() != null) {
				order.setShippingAddress(mapToShippingAddress(request.getShippingAddress()));
			}
			if (request.getPaymentMethod() != null) {
				order.setPaymentMethod(request.getPaymentMethod());
			}
		}

		List<OrderItem> orderItems = cartItems.stream().map(item -> {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProductId(item.getProductId());
			orderItem.setProductName(item.getProductName());
			orderItem.setProductImage(item.getProductImage());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setPrice(item.getPrice());
			return orderItem;
		}).collect(java.util.stream.Collectors.toList());

		order.setItems(orderItems);
		Order savedOrder = orderRepository.save(order);

		rabbitTemplate.convertAndSend(exchangeName, routingKey,
				order);

		// Clear the cart
		cartService.clearCart(userId);

		return Optional.of(mapToOrderResponse(savedOrder));
	}

	private OrderResponse mapToOrderResponse(Order order) {
		Map<String, ProductResponse> productCache = new HashMap<>();
		List<OrderItemDTO> items = order.getItems().stream().map(orderItem -> {
			String productName = orderItem.getProductName();
			String imageUrl = orderItem.getProductImage();
			boolean needsLookup = (productName == null || productName.isBlank())
					|| (imageUrl == null || imageUrl.isBlank());
			ProductResponse product = needsLookup ? getProductDetailsCached(orderItem.getProductId(), productCache)
					: null;

			if (productName == null || productName.isBlank()) {
				productName = product != null ? product.getName() : null;
			}
			if (imageUrl == null || imageUrl.isBlank()) {
				imageUrl = resolveProductImage(product);
			}

			return new OrderItemDTO(orderItem.getId(), orderItem.getProductId(), productName, imageUrl,
					orderItem.getQuantity(), orderItem.getPrice(),
					orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
		}).toList();

		return new OrderResponse(order.getId(), order.getUserId(), order.getTotalAmount(), order.getStatus(), items,
				order.getShippingAddress() != null ? mapToShippingAddressDTO(order.getShippingAddress()) : null,
				order.getPaymentMethod(),
				order.getPaymentResult() != null ? mapToPaymentResultDTO(order.getPaymentResult()) : null,
				order.getItemsPrice(), order.getTaxPrice(), order.getShippingPrice(), order.getIsPaid(),
				order.getPaidAt(), order.getIsDelivered(), order.getDeliveredAt(), order.getPaypalOrderId(),
				order.getStripeClientSecret(), order.getCreatedAt(), order.getUpdatedAt());
	}

	private ProductResponse getProductDetailsCached(String productId, Map<String, ProductResponse> cache) {
		if (cache.containsKey(productId)) {
			return cache.get(productId);
		}
		try {
			ProductResponse product = productServiceClient.getProductDetails(productId);
			cache.put(productId, product);
			return product;
		} catch (Exception e) {
			cache.put(productId, null);
			return null;
		}
	}

	private String resolveProductImage(ProductResponse product) {
		if (product == null) {
			return null;
		}
		if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
			return product.getImageUrl();
		}
		if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
			return product.getImageUrls().get(0);
		}
		return null;
	}

	private com.ecommerce.order.dtos.ShippingAddressDTO mapToShippingAddressDTO(
			com.ecommerce.order.models.ShippingAddress address) {
		return new com.ecommerce.order.dtos.ShippingAddressDTO(address.getFirstName(), address.getLastName(),
				address.getStreet(), address.getCity(), address.getState(), address.getZipCode(), address.getCountry(),
				address.getPhone());
	}

	private ShippingAddress mapToShippingAddress(ShippingAddressDTO address) {
		if (address == null) {
			return null;
		}
		return new ShippingAddress(address.getFirstName(), address.getLastName(), address.getStreet(),
				address.getCity(), address.getState(), address.getZipCode(), address.getCountry(), address.getPhone());
	}

	private com.ecommerce.order.dtos.PaymentResultDTO mapToPaymentResultDTO(
			com.ecommerce.order.models.PaymentResult payment) {
		return new com.ecommerce.order.dtos.PaymentResultDTO(payment.getPaymentId(), payment.getStatus(),
				payment.getUpdateTime(), payment.getEmailAddress());
	}

	private BigDecimal safeMoney(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private BigDecimal money(BigDecimal value) {
		return safeMoney(value).setScale(2, RoundingMode.HALF_UP);
	}

	// Get order by ID
	public Optional<OrderResponse> getOrderById(Long id) {
		return orderRepository.findById(id).map(this::mapToOrderResponse);
	}

	// Get all orders (admin)
	public List<OrderResponse> getAllOrders() {
		return orderRepository.findAll().stream().map(this::mapToOrderResponse).toList();
	}

	// Get user's orders
	public List<OrderResponse> getUserOrders(String userId) {
		return orderRepository.findByUserId(userId).stream().map(this::mapToOrderResponse).toList();
	}

	// Mark order as paid
	public Optional<OrderResponse> markAsPaid(Long id, com.ecommerce.order.dtos.PaymentResultDTO paymentResult) {
		return orderRepository.findById(id).map(order -> {
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
		}).map(this::mapToOrderResponse);
	}

	// Mark order as delivered (admin)
	public Optional<OrderResponse> markAsDelivered(Long id) {
		return orderRepository.findById(id).map(order -> {
			order.setIsDelivered(true);
			order.setDeliveredAt(java.time.LocalDateTime.now());
			return orderRepository.save(order);
		}).map(this::mapToOrderResponse);
	}
}
