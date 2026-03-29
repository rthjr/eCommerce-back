package com.ecommerce.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecommerce.notification.dto.UserDTO;

@FeignClient(name = "user-service", url = "${app.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") String userId);
}
