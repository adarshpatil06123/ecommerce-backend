package com.ecommerce.order.kafka;

import com.ecommerce.common.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "order-created-topic";

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📤 Publishing order-created event for Order ID: {} | User: {} | Product: {} | Amount: ${} | Quantity: {}", 
                event.getOrderId(), event.getUserId(), event.getProductId(), event.getAmount(), event.getQuantity());
        
        try {
            CompletableFuture<SendResult<String, OrderCreatedEvent>> future = 
                kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Successfully published order-created event | Order ID: {} | Topic: {} | Partition: {} | Offset: {}", 
                            event.getOrderId(), TOPIC, 
                            result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish order-created event | Order ID: {} | Error: {}", 
                            event.getOrderId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("❌ Exception while publishing order-created event | Order ID: {} | Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}
