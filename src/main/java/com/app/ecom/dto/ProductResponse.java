package com.app.ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProductResponse {
	
	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("price")
	private Double price;
	
	@JsonProperty("stock_quantity")
	private Integer stockQuantity;
	
	@JsonProperty("category")
	private String category;
	
	@JsonProperty("image_url")
	private String imageUrl;
	
	@JsonProperty("active")
	private Boolean active;
}
