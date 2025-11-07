package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "${services.product.url}")
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ApiResponse<ProductDTO> getProductById(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/check-stock")
    ApiResponse<Boolean> checkStock(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);

    @PostMapping("/products/{productId}/reduce-stock")
    ApiResponse<ProductDTO> reduceStock(@PathVariable("productId") Long productId, @RequestBody StockUpdateDTO stockUpdate);
}
