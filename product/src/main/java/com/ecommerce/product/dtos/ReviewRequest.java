package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class ReviewRequest {
	private Integer rating;
	private String content;
	private String userId;
}
