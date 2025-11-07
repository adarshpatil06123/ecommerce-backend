package com.ecommerce.payment.kafka;

import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created-topic", groupId = "ecommerce_payment_group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("📥 Received order-created event | Order ID: {} | User: {} | Product: {} | Amount: ${} | Quantity: {}", 
                event.getOrderId(), event.getUserId(), event.getProductId(), event.getAmount(), event.getQuantity());
        
        try {
            // Process payment asynchronously
            paymentService.processOrderPayment(event);
            log.info("✅ Successfully processed order-created event | Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Failed to process order-created event | Order ID: {} | Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
            // In production, you might want to:
            // 1. Send to a Dead Letter Queue (DLQ)
            // 2. Trigger retry mechanism
            // 3. Send alert/notification
        }
    }
}
