package com.ecommerce.product.specifications;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.product.models.Product;

public class ProductSpecification {

	public static Specification<Product> hasCategory(String category) {
		return (root, query, criteriaBuilder) -> category == null ? null
				: criteriaBuilder.equal(root.get("category"), category);
	}

	public static Specification<Product> hasColorIn(List<String> colors) {
		return (root, query, criteriaBuilder) -> (colors == null || colors.isEmpty()) ? null
				: root.join("colors").in(colors);
	}

	public static Specification<Product> hasSizeIn(List<String> sizes) {
		return (root, query, criteriaBuilder) -> (sizes == null || sizes.isEmpty()) ? null
				: root.join("sizes").in(sizes);
	}

	public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
		return (root, query, criteriaBuilder) -> {
			if (minPrice == null && maxPrice == null)
				return null;
			if (minPrice != null && maxPrice != null) {
				return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
			} else if (minPrice != null) {
				return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
			} else {
				return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
			}

		};
	}

}