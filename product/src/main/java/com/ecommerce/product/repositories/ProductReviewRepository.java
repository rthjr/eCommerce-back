package com.ecommerce.product.repositories;

import com.ecommerce.product.models.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
	Page<ProductReview> findByProductId(Long productId, Pageable pageable);

	Long countByProductId(Long productId);

	boolean existsByProductIdAndUserId(Long productId, Long userId);
}
