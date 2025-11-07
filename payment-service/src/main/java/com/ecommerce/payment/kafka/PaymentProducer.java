package com.ecommerce.payment.kafka;

import com.ecommerce.common.events.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private static final String TOPIC = "payment-completed-topic";

    public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("📤 Publishing payment-completed event for Order ID: {} | Status: {} | Transaction: {}", 
                event.getOrderId(), event.getStatus(), event.getTransactionId());
        
        try {
            CompletableFuture<SendResult<String, PaymentCompletedEvent>> future = 
                kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Successfully published payment-completed event | Order ID: {} | Topic: {} | Partition: {} | Offset: {}", 
                            event.getOrderId(), TOPIC, 
                            result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish payment-completed event | Order ID: {} | Error: {}", 
                            event.getOrderId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("❌ Exception while publishing payment-completed event | Order ID: {} | Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}
