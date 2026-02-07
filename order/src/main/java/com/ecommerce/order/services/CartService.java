package com.ecommerce.order.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dtos.CartItemRequest;
import com.ecommerce.order.dtos.CartItemResponse;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repositories.CartItemRepository;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
	private final CartItemRepository cartItemRepository;
	private final ProductServiceClient productServiceClient;
	private final UserServiceClient userServiceClient;
	int attempt = 0;

//	 @CircuitBreaker(name = "productService")
//	@CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallBack")
	@Retry(name = "retryBreaker", fallbackMethod = "addToCartFallBack")
	public boolean addToCart(String userId, CartItemRequest request) {
		System.out.println("ATTEMPT COUNT: " + ++attempt);
		ProductResponse productResponse = productServiceClient.getProductDetails(request.getProductId());
		if (productResponse == null || productResponse.getStockQuantity() < request.getQuantity())
			return false;

		UserResponse userResponse = userServiceClient.getUserDetails(userId);
		if (userResponse == null) {
			return false;
		}

		CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
		if (existingCartItem != null) {
			// Update the quantity
			existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
			existingCartItem.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.valueOf(1000.00));

			// Update product details if provided
			if (request.getProductName() != null) {
				existingCartItem.setProductName(request.getProductName());
			}
			if (request.getProductImage() != null) {
				existingCartItem.setProductImage(request.getProductImage());
			}
			if (request.getSelectedColor() != null) {
				existingCartItem.setSelectedColor(request.getSelectedColor());
			}
			if (request.getSelectedSize() != null) {
				existingCartItem.setSelectedSize(request.getSelectedSize());
			}

			cartItemRepository.save(existingCartItem);
		} else {
			// Create new cart item with product details
			CartItem cartItem = new CartItem();
			cartItem.setUserId(userId);
			cartItem.setProductId(request.getProductId());
			cartItem.setQuantity(request.getQuantity());
			cartItem.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.valueOf(1000.00));

			// Set product details
			cartItem.setProductName(request.getProductName());
			cartItem.setProductImage(request.getProductImage());
			cartItem.setSelectedColor(request.getSelectedColor());
			cartItem.setSelectedSize(request.getSelectedSize());

			cartItemRepository.save(cartItem);
		}
		return true;
	}

	public boolean addToCartFallBack(String userId, CartItemRequest request, Exception exception) {
		exception.printStackTrace();
		return true;
	}

	public boolean deleteItemFromCart(String userId, String productId) {
		CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

		if (cartItem != null) {
			cartItemRepository.delete(cartItem);
			return true;
		}
		return false;
	}

	public List<CartItem> getCart(String userId) {
		return cartItemRepository.findByUserId(userId);
	}

	// Get cart with full product details
	public List<CartItemResponse> getCartWithDetails(String userId) {
		return cartItemRepository.findByUserId(userId).stream().map(this::mapToCartItemResponse)
				.collect(Collectors.toList());
	}

	private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
		return new CartItemResponse(cartItem.getId(), cartItem.getProductId(), cartItem.getProductName(),
				cartItem.getProductImage(), cartItem.getQuantity(), cartItem.getPrice(), cartItem.getSelectedColor(),
				cartItem.getSelectedSize());
	}

	public void clearCart(String userId) {
		cartItemRepository.deleteByUserId(userId);
	}

}
