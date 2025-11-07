package com.ecommerce.product.service;

import com.ecommerce.common.exception.InsufficientStockException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    @CacheEvict(value = "productList", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .build();

        product = productRepository.save(product);
        log.info("Product created with ID: {}, clearing productList cache", product.getId());
        return mapToResponse(product);
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProductById(Long productId) {
        log.info("Fetching product from database for ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToResponse(product);
    }

    @Cacheable(value = "productList")
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "productSearch", key = "#name")
    public List<ProductResponse> searchProducts(String name) {
        log.info("Searching products in database for name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "productSearch", allEntries = true)
    })
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        product = productRepository.save(product);
        log.info("Product updated, clearing caches for ID: {}", productId);
        return mapToResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "productSearch", allEntries = true)
    })
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        productRepository.deleteById(productId);
        log.info("Product deleted, clearing caches for ID: {}", productId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public ProductResponse reduceStock(Long productId, Integer quantity) {
        log.info("Reducing stock for product ID: {} by quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                            product.getName(), product.getStock(), quantity)
            );
        }

        product.setStock(product.getStock() - quantity);
        product = productRepository.save(product);
        log.info("Stock reduced, clearing cache for product ID: {}", productId);

        return mapToResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public ProductResponse addStock(Long productId, Integer quantity) {
        log.info("Adding stock for product ID: {} by quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setStock(product.getStock() + quantity);
        product = productRepository.save(product);
        log.info("Stock added, clearing cache for product ID: {}", productId);

        return mapToResponse(product);
    }

    @Cacheable(value = "productStock", key = "#productId")
    public boolean checkStock(Long productId, Integer quantity) {
        log.info("Checking stock from database for product ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return product.getStock() >= quantity;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public ProductResponse updateProductImage(Long productId, String imageUrl) {
        log.info("Updating product image for ID: {} with URL: {}", productId, imageUrl);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setImageUrl(imageUrl);
        product = productRepository.save(product);
        log.info("Image updated, clearing cache for product ID: {}", productId);

        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
