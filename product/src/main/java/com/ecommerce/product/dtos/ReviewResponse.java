package com.ecommerce.product.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewResponse {
	private Long id;
	private String userId;
	private String user;
	private String content;
	private Integer rating;
	private LocalDateTime date;
	private Boolean verifiedPurchase;
	private Integer helpfulCount;
}
