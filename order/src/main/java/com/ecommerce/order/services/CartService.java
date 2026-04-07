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
		
		// Try to validate product, but allow adding even if product service is unavailable
		try {
			ProductResponse productResponse = productServiceClient.getProductDetails(request.getProductId());
			if (productResponse != null && productResponse.getStockQuantity() < request.getQuantity()) {
				System.out.println("Product out of stock: " + request.getProductId());
				return false;
			}
		} catch (Exception e) {
			System.out.println("Product service unavailable, proceeding with cart add: " + e.getMessage());
			// Continue with cart add even if product service is down
		}

		// Try to validate user, but allow adding even if user service is unavailable
		try {
			UserResponse userResponse = userServiceClient.getUserDetails(userId);
			if (userResponse == null) {
				System.out.println("User not found but proceeding: " + userId);
				// Don't block cart add if user service is down
			}
		} catch (Exception e) {
			System.out.println("User service unavailable, proceeding with cart add: " + e.getMessage());
			// Continue with cart add even if user service is down
		}

		CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
		if (existingCartItem != null) {
			// Replace the quantity instead of adding to it
			// This allows checkout to resync cart by re-posting all items
			existingCartItem.setQuantity(request.getQuantity());
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
		System.out.println("addToCartFallBack triggered for userId: " + userId + ", productId: " + request.getProductId());
		exception.printStackTrace();
		
		// Actually save the cart item in fallback
		try {
			CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
			if (existingCartItem != null) {
				existingCartItem.setQuantity(request.getQuantity());
				existingCartItem.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.valueOf(1000.00));
				if (request.getProductName() != null) existingCartItem.setProductName(request.getProductName());
				if (request.getProductImage() != null) existingCartItem.setProductImage(request.getProductImage());
				if (request.getSelectedColor() != null) existingCartItem.setSelectedColor(request.getSelectedColor());
				if (request.getSelectedSize() != null) existingCartItem.setSelectedSize(request.getSelectedSize());
				cartItemRepository.save(existingCartItem);
			} else {
				CartItem cartItem = new CartItem();
				cartItem.setUserId(userId);
				cartItem.setProductId(request.getProductId());
				cartItem.setQuantity(request.getQuantity());
				cartItem.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.valueOf(1000.00));
				cartItem.setProductName(request.getProductName());
				cartItem.setProductImage(request.getProductImage());
				cartItem.setSelectedColor(request.getSelectedColor());
				cartItem.setSelectedSize(request.getSelectedSize());
				cartItemRepository.save(cartItem);
			}
			return true;
		} catch (Exception e) {
			System.out.println("Fallback also failed: " + e.getMessage());
			return false;
		}
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
