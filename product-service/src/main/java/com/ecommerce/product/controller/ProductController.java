package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long productId) {
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(@RequestParam String name) {
        List<ProductResponse> products = productService.searchProducts(name);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @PostMapping("/{productId}/reduce-stock")
    public ResponseEntity<ApiResponse<ProductResponse>> reduceStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductResponse product = productService.reduceStock(productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Stock reduced successfully", product));
    }

    @PostMapping("/{productId}/add-stock")
    public ResponseEntity<ApiResponse<ProductResponse>> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductResponse product = productService.addStock(productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", product));
    }

    @GetMapping("/{productId}/check-stock")
    public ResponseEntity<ApiResponse<Boolean>> checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        boolean isAvailable = productService.checkStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(isAvailable));
    }

    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        
        // Upload file to S3
        String imageUrl = s3Service.uploadFile(file, "products/product-" + productId);
        
        // Update product with new image URL
        ProductResponse product = productService.updateProductImage(productId, imageUrl);
        
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", product));
    }
}

