package com.ecommerce.order.service;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.order.client.AuthServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.client.StockUpdateDTO;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductDTO;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.kafka.OrderProducer;
import com.ecommerce.order.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuthServiceClient authServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderProducer orderProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {} and product: {}", request.getUserId(), request.getProductId());

        // Step 1: Validate user exists
        try {
            authServiceClient.getUserById(request.getUserId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        } catch (FeignException e) {
            throw new BadRequestException("Failed to validate user: " + e.getMessage());
        }

        // Step 2: Get product details and check stock
        ProductDTO product;
        try {
            var productResponse = productServiceClient.getProductById(request.getProductId());
            product = productResponse.getData();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product", "id", request.getProductId());
        } catch (FeignException e) {
            throw new BadRequestException("Failed to fetch product details: " + e.getMessage());
        }

        // Step 3: Check stock availability
        try {
            var stockCheckResponse = productServiceClient.checkStock(request.getProductId(), request.getQuantity());
            if (!stockCheckResponse.getData()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
        } catch (FeignException e) {
            throw new BadRequestException("Failed to check stock: " + e.getMessage());
        }

        // Step 4: Calculate total amount
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Step 5: Create order
        Order order = Order.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);

        // Step 6: Reduce stock
        try {
            StockUpdateDTO stockUpdate = StockUpdateDTO.builder()
                    .quantity(request.getQuantity())
                    .build();
            productServiceClient.reduceStock(request.getProductId(), stockUpdate);
            
            // Update order status to CONFIRMED
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            // Step 7: Publish order-created event to Kafka
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .productId(order.getProductId())
                    .amount(totalAmount.doubleValue())
                    .quantity(order.getQuantity())
                    .build();
            
            orderProducer.sendOrderCreatedEvent(event);
            
        } catch (FeignException e) {
            log.error("Failed to reduce stock: {}", e.getMessage());
            throw new BadRequestException("Failed to reduce stock: " + e.getMessage());
        }

        log.info("Order created successfully with ID: {}", order.getId());
        return mapToResponse(order);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order {} status to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel order in " + order.getStatus() + " status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Optionally: Add stock back to product
        // This would require an "add-stock" call to product service
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
