package com.ecommerce.product.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "products")
@Data
@NoArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stockQuantity;
	private String category;
	private Long sellerId;
	private String sellerName;

	@Deprecated
	private String imageUrl; // Deprecated: use imageUrls instead

	private Boolean active = true;

	// Additional fields
	private String brand;
	private Double rating = 0.0;
	private Integer numReviews = 0;
	private BigDecimal discountPrice;

	// Multiple images support
	@ElementCollection
	@CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "image_url")
	private List<String> imageUrls = new ArrayList<>();

	// additional field for filter

	@ElementCollection
	@CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "size")
	private List<String> sizes;

	@ElementCollection
	@CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "color")
	private List<String> colors;

	private String dressStyle;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductReview> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductFAQ> faqs = new ArrayList<>();
}
