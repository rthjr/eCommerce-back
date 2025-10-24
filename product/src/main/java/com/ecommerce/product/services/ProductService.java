package com.ecommerce.product.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.ecommerce.product.dtos.FAQRequest;
import com.ecommerce.product.dtos.FAQResponse;
import com.ecommerce.product.dtos.ProductRequest;
import com.ecommerce.product.dtos.ProductResponse;
import com.ecommerce.product.dtos.ReviewRequest;
import com.ecommerce.product.dtos.ReviewResponse;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.ProductFAQ;
import com.ecommerce.product.models.ProductReview;
import com.ecommerce.product.repositories.ProductFAQRepository;
import com.ecommerce.product.repositories.ProductRepository;
import com.ecommerce.product.repositories.ProductReviewRepository;
import com.ecommerce.product.specifications.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductReviewRepository reviewRepository;
	private final ProductFAQRepository faqRepository;

	public ProductResponse createProduct(ProductRequest productRequest, String sellerId, String sellerName) {
		Product product = new Product();
		updateProductFromRequest(product, productRequest);
		
		// Set seller information
		if (sellerId != null && !sellerId.isEmpty()) {
			product.setSellerId(sellerId);
		}
		if (sellerName != null && !sellerName.isEmpty()) {
			product.setSellerName(sellerName);
		}
		
		Product savedProduct = productRepository.save(product);
		return mapToProductResponse(savedProduct);
	}

	public List<ProductResponse> getOwnerProducts(String sellerId) {
	    return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
	        .map(this::mapToProductResponse)
	        .collect(Collectors.toList());
	}

	
	private ProductResponse mapToProductResponse(Product savedProduct) {
		ProductResponse response = new ProductResponse();
		response.setId(savedProduct.getId());
		response.setName(savedProduct.getName());
		response.setActive(savedProduct.getActive());
		response.setCategory(savedProduct.getCategory());
		response.setDescription(savedProduct.getDescription());
		response.setPrice(savedProduct.getPrice());
		response.setStockQuantity(savedProduct.getStockQuantity());
		response.setSellerId(savedProduct.getSellerId());
		response.setSellerName(savedProduct.getSellerName());

		// Set new fields
		response.setBrand(savedProduct.getBrand());
		response.setRating(savedProduct.getRating());
		response.setNumReviews(savedProduct.getNumReviews());
		response.setDiscountPrice(savedProduct.getDiscountPrice());
		response.setImageUrls(savedProduct.getImageUrls());
		response.setSizes(savedProduct.getSizes());
		response.setColors(savedProduct.getColors());
		response.setDressStyle(savedProduct.getDressStyle());
		response.setCreatedAt(savedProduct.getCreatedAt());
		response.setUpdatedAt(savedProduct.getUpdatedAt());

		// Set deprecated imageUrl for backward compatibility
		if (savedProduct.getImageUrls() != null && !savedProduct.getImageUrls().isEmpty()) {
			response.setImageUrl(savedProduct.getImageUrls().get(0));
		} else if (savedProduct.getImageUrl() != null) {
			response.setImageUrl(savedProduct.getImageUrl());
		}

		return response;
	}

	private void updateProductFromRequest(Product product, ProductRequest productRequest) {
		product.setName(productRequest.getName());
		product.setCategory(productRequest.getCategory());
		product.setDescription(productRequest.getDescription());
		product.setPrice(productRequest.getPrice());
		product.setStockQuantity(productRequest.getStockQuantity());
		
		// Update active status if provided
		if (productRequest.getActive() != null) {
			product.setActive(productRequest.getActive());
		}

		// Handle image URL migration
		if (productRequest.getImageUrl() != null) {
			// For backward compatibility, add to imageUrls list
			product.getImageUrls().clear();
			product.getImageUrls().add(productRequest.getImageUrl());
		}
	}

	public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
		return productRepository.findById(id).map(existingProduct -> {
			// TODO: Add ownership validation in controller
			updateProductFromRequest(existingProduct, productRequest);
			Product savedProduct = productRepository.save(existingProduct);
			return mapToProductResponse(savedProduct);
		});
	}

	public List<ProductResponse> getAllProducts() {
		return productRepository.findByActiveTrue().stream().map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}

	public boolean deleteProduct(Long id) {
		return productRepository.findById(id).map(product -> {
			// TODO: Add ownership validation in controller
			product.setActive(false);
			productRepository.save(product);
			return true;
		}).orElse(false);
	}

	public List<ProductResponse> searchProducts(String keyword) {
		return productRepository.searchProducts(keyword).stream().map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}

	public Page<Product> filterProducts(String category, List<String> colors, List<String> sized, BigDecimal minPrice,
			BigDecimal maxPrize, String sortBy, String sortDirection, int page, int size) {
		Specification<Product> spec = Specification.where(ProductSpecification.hasCategory(category))
				.and(ProductSpecification.hasColorIn(colors)).and(ProductSpecification.hasSizeIn(sized))
				.and(ProductSpecification.priceBetween(minPrice, maxPrize));

		Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);

		Pageable pageable = PageRequest.of(page, size, sort);

		return productRepository.findAll(spec, pageable);
	}

	public Optional<ProductResponse> getProductById(Long id) {
		return productRepository.findByIdAndActiveTrue(id).map(this::mapToProductResponse);
	}

	// Get top products by rating
	public List<ProductResponse> getTopProducts(int limit) {
		Pageable pageable = PageRequest.of(0, limit, Sort.by("rating").descending());
		return productRepository.findByActiveTrue(pageable).stream()
				.map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}

	// Add these methods to your existing ProductService

	public Page<ReviewResponse> getProductReviews(Long productId, int page, int size, String sortBy) {
		Sort sort = switch (sortBy) {
			case "oldest" -> Sort.by("date").ascending();
			case "most-relevant" -> Sort.by("verifiedPurchase").descending().and(Sort.by("helpfulCount").descending());
			default -> Sort.by("date").descending(); // latest
		};

		Pageable pageable = PageRequest.of(page, size, sort);
		Page<ProductReview> reviews = reviewRepository.findByProductId(productId, pageable);

		return reviews.map(this::mapToReviewResponse);
	}

	public ReviewResponse createReview(Long productId, ReviewRequest request) {
		if (reviewRepository.existsByProductIdAndUserId(productId, request.getUserId())) {
			throw new RuntimeException("User already reviewed this product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		ProductReview review = new ProductReview();
		review.setProduct(product);
		review.setRating(request.getRating());
		review.setContent(request.getContent());
		review.setUserId(request.getUserId());
		review.setUser("User " + request.getUserId()); // Simplified user display

		ProductReview saved = reviewRepository.save(review);
		return mapToReviewResponse(saved);
	}

	public List<FAQResponse> getProductFAQs(Long productId) {
		return faqRepository.findByProductIdOrderByOrderIndexAsc(productId).stream().map(this::mapToFAQResponse)
				.collect(Collectors.toList());
	}

	private ReviewResponse mapToReviewResponse(ProductReview review) {
		ReviewResponse response = new ReviewResponse();
		response.setId(review.getId());
		response.setUserId(review.getUserId());
		response.setUser(review.getUser());
		response.setContent(review.getContent());
		response.setRating(review.getRating());
		response.setDate(review.getDate());
		response.setVerifiedPurchase(review.getVerifiedPurchase());
		response.setHelpfulCount(review.getHelpfulCount());
		return response;
	}

	private FAQResponse mapToFAQResponse(ProductFAQ faq) {
		FAQResponse response = new FAQResponse();
		response.setId(faq.getId());
		response.setProductId(faq.getProduct().getId());
		response.setQuestion(faq.getQuestion());
		response.setAnswer(faq.getAnswer());
		response.setOrder(faq.getOrderIndex());
		return response;
	}

	public FAQResponse createFAQ(Long productId, FAQRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		ProductFAQ faq = new ProductFAQ();
		faq.setProduct(product);
		faq.setQuestion(request.getQuestion());
		faq.setAnswer(request.getAnswer());
		faq.setOrderIndex(request.getOrder());

		ProductFAQ saved = faqRepository.save(faq);
		return mapToFAQResponse(saved);
	}

	public List<ProductResponse> getProductsBySellerId(String sellerId) {
		return productRepository.findBySellerId(sellerId).stream()
				.map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}
	
	// In ProductService
	public boolean reduceStock(Long productId, Integer quantity) {
	    return productRepository.findById(productId)
	        .map(product -> {
	            if (product.getStockQuantity() >= quantity) {
	                product.setStockQuantity(product.getStockQuantity() - quantity);
	                productRepository.save(product);
	                return true;
	            }
	            return false; // Insufficient stock
	        })
	        .orElse(false); // Product not found
	}


}
