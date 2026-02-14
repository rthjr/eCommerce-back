package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ProductFAQ {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String question;
	private String answer;
	private Integer orderIndex;
	private Boolean hidden = false;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
}
