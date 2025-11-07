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
public class PaymentCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private String status;
    private String transactionId;
}
