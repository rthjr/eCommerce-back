package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class FAQResponse {
	private Long id;
	private Long productId;
	private String question;
	private String answer;
	private Integer order;
}
