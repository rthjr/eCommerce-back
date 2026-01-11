package com.ecommerce.product.dtos;

import lombok.Data;

@Data
public class FAQRequest {
	private String question;
	private String answer;
	private Integer order;
}
