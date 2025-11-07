package com.ecommerce.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private Long userId;
    private Long productId;
    private Double amount;
    private Integer quantity;
}
