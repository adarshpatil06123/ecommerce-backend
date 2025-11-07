package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthServiceClient {

    @GetMapping("/auth/users/{userId}")
    ApiResponse<Object> getUserById(@PathVariable("userId") Long userId);
}
