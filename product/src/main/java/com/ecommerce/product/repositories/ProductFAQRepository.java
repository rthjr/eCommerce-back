package com.ecommerce.product.repositories;

import com.ecommerce.product.models.ProductFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductFAQRepository extends JpaRepository<ProductFAQ, Long> {
	List<ProductFAQ> findByProductIdOrderByOrderIndexAsc(Long productId);

	List<ProductFAQ> findByProductIdAndHiddenFalseOrderByOrderIndexAsc(Long productId);

	@Query("SELECT f FROM ProductFAQ f JOIN f.product p WHERE p.sellerId = :sellerId")
	List<ProductFAQ> findByProductSellerId(@Param("sellerId") String sellerId);

	@Query("SELECT f FROM ProductFAQ f JOIN f.product p WHERE p.sellerId = :sellerId AND (f.answer IS NULL OR f.answer = '')")
	List<ProductFAQ> findUnansweredBySellerId(@Param("sellerId") String sellerId);

	@Query("SELECT f FROM ProductFAQ f JOIN f.product p WHERE p.sellerId = :sellerId AND f.hidden = true")
	List<ProductFAQ> findHiddenBySellerId(@Param("sellerId") String sellerId);
}
