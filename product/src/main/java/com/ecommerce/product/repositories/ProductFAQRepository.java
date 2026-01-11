package com.ecommerce.product.repositories;

import com.ecommerce.product.models.ProductFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductFAQRepository extends JpaRepository<ProductFAQ, Long> {
	List<ProductFAQ> findByProductIdOrderByOrderIndexAsc(Long productId);
}
