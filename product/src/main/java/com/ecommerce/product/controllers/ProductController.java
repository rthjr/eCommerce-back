package com.ecommerce.product.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.dtos.FAQRequest;
import com.ecommerce.product.dtos.FAQResponse;
import com.ecommerce.product.dtos.ProductRequest;
import com.ecommerce.product.dtos.ProductResponse;
import com.ecommerce.product.dtos.ReviewRequest;
import com.ecommerce.product.dtos.ReviewResponse;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.services.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest,
			@RequestHeader(value = "X-User-Id", required = false) String sellerId,
			@RequestHeader(value = "X-User-Name", required = false) String sellerName) {
		return new ResponseEntity<ProductResponse>(productService.createProduct(productRequest, sellerId, sellerName),
				HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(required = false) String sellerId) {
		if (sellerId != null && !sellerId.isEmpty()) {
			return ResponseEntity.ok(productService.getProductsBySellerId(sellerId));
		}
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@GetMapping("/owner")
	public ResponseEntity<List<ProductResponse>> getOwnerProducts(
			@RequestHeader(value = "X-User-Id", required = true) String sellerId) {
		return ResponseEntity.ok(productService.getOwnerProducts(sellerId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
	        @RequestBody ProductRequest productRequest) {
	    return productService.updateProduct(id, productRequest)
	        .map(ResponseEntity::ok)
	        .orElseGet(() -> ResponseEntity.notFound().build());
	}



	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		boolean deleted = productService.deleteProduct(id);
		return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
		return ResponseEntity.ok(productService.searchProducts(keyword));
	}

	@GetMapping("/filter")
	public Page<Product> filterProducts(@RequestParam(required = false) String category,
			@RequestParam(required = false) List<String> colors, @RequestParam(required = false) List<String> sizes,
			@RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(defaultValue = "price") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return productService.filterProducts(category, colors, sizes, minPrice, maxPrice, sortBy, sortDirection, page,
				size);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return productService.getProductById(id).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/top")
	public ResponseEntity<List<ProductResponse>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
		return ResponseEntity.ok(productService.getTopProducts(limit));
	}

	// Add these endpoints to your existing ProductController

	@GetMapping("/{productId}/reviews")
	public ResponseEntity<Page<ReviewResponse>> getProductReviews(@PathVariable Long productId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(defaultValue = "latest") String sortBy) {
		return ResponseEntity.ok(productService.getProductReviews(productId, page, size, sortBy));
	}

	@PostMapping("/{productId}/reviews")
	public ResponseEntity<ReviewResponse> createReview(@PathVariable Long productId,
			@RequestBody ReviewRequest reviewRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.createReview(productId, reviewRequest));
	}

	@GetMapping("/{productId}/faqs")
	public ResponseEntity<List<FAQResponse>> getProductFAQs(@PathVariable Long productId) {
		return ResponseEntity.ok(productService.getProductFAQs(productId));
	}

	@PostMapping("/{productId}/faqs")
	public ResponseEntity<FAQResponse> createFAQ(@PathVariable Long productId, @RequestBody FAQRequest faqRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.createFAQ(productId, faqRequest));
	}

	@GetMapping("/seller/{sellerId}")
	public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable String sellerId) {
		return ResponseEntity.ok(productService.getProductsBySellerId(sellerId));
	}
	
	// In ProductController
	@PutMapping("/{id}/reduce-stock")
	public ResponseEntity<Void> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
	    boolean reduced = productService.reduceStock(id, quantity);
	    return reduced ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
	}


}
