package com.ecommerce.product.repositories;

import com.ecommerce.product.models.ProductReview;
import com.ecommerce.product.models.ProductReview.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
	Page<ProductReview> findByProductId(Long productId, Pageable pageable);

	Page<ProductReview> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

	Long countByProductId(Long productId);

	boolean existsByProductIdAndUserId(Long productId, String userId);

	@Query("SELECT r FROM ProductReview r WHERE r.product.id = :productId AND r.userId = :userId")
	java.util.Optional<ProductReview> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") String userId);

	Long countByProductIdAndStatus(Long productId, ReviewStatus status);

	@Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.status = :status")
	Double getAverageRatingByProductIdAndStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status);

	@Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
	Double getAverageRatingByProductId(@Param("productId") Long productId);
	
	// Seller feedback management queries
	@Query("SELECT r FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId")
	Page<ProductReview> findByProductSellerId(@Param("sellerId") String sellerId, Pageable pageable);
	
	@Query("SELECT r FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId")
	List<ProductReview> findAllByProductSellerId(@Param("sellerId") String sellerId);
	
	@Query("SELECT r FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId AND r.sellerResponse IS NULL")
	List<ProductReview> findUnansweredByProductSellerId(@Param("sellerId") String sellerId);
	
	@Query("SELECT r FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId AND r.isFlagged = true")
	List<ProductReview> findFlaggedByProductSellerId(@Param("sellerId") String sellerId);
	
	@Query("SELECT r FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId AND r.status = :status")
	List<ProductReview> findByProductSellerIdAndStatus(@Param("sellerId") String sellerId, @Param("status") ReviewStatus status);
	
	@Query("SELECT AVG(r.rating) FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId")
	Double getAverageRatingBySellerId(@Param("sellerId") String sellerId);
	
	@Query("SELECT COUNT(r) FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId")
	Long countByProductSellerId(@Param("sellerId") String sellerId);
	
	@Query("SELECT COUNT(r) FROM ProductReview r JOIN r.product p WHERE p.sellerId = :sellerId AND r.sellerResponse IS NULL")
	Long countUnansweredByProductSellerId(@Param("sellerId") String sellerId);
}
