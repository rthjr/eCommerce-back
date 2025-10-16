package com.app.ecom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity(name = "PRODUCTS")
public class Product extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Column(name = "DESCRIPTION", length = 1000)
	private String description;
	
	@Column(name = "PRICE", nullable = false)
	private Double price;
	
	@Column(name = "STOCK_QUANTITY", nullable = false)
	private Integer stockQuantity;
	
	@Column(name = "CATEGORY")
	private String category;
	
	@Column(name = "IMAGE_URL")
	private String imageUrl;
	
	@Column(name = "ACTIVE", nullable = false)
	private Boolean active = true;
}
