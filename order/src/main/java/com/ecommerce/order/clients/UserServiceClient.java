package com.ecommerce.order.clients;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.ecommerce.order.dtos.UserResponse;

@HttpExchange
public interface UserServiceClient {
	
	@GetExchange("/api/users/{id}")
	UserResponse getUserDetails(@PathVariable String id);
}
