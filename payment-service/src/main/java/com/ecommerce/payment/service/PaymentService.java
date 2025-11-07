package com.ecommerce.payment.service;

import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.kafka.PaymentProducer;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final Random random = new Random();

    @Value("${payment.success-rate:0.8}")
    private double successRate;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Payment already exists for order ID: " + request.getOrderId());
        }

        // Simulate payment processing
        boolean isSuccess = simulatePayment();
        PaymentStatus status = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String transactionId = isSuccess ? generateTransactionId() : null;
        String remarks = isSuccess ? "Payment processed successfully" : "Payment failed due to insufficient funds or invalid card";

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(status)
                .transactionId(transactionId)
                .paymentMethod(request.getPaymentMethod())
                .remarks(remarks)
                .build();

        payment = paymentRepository.save(payment);

        log.info("Payment {} for order ID: {} with transaction ID: {}", 
                status, request.getOrderId(), transactionId);

        return mapToResponse(payment);
    }

    /**
     * Process payment for order created event from Kafka
     * This method is called asynchronously when an order is created
     */
    @Transactional
    public void processOrderPayment(OrderCreatedEvent event) {
        log.info("🔄 Processing payment for order from Kafka event | Order ID: {} | Amount: ${}", 
                event.getOrderId(), event.getAmount());

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("⚠️ Payment already exists for order ID: {}", event.getOrderId());
            return;
        }

        // Simulate payment processing
        boolean isSuccess = simulatePayment();
        PaymentStatus status = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String transactionId = isSuccess ? generateTransactionId() : null;
        String remarks = isSuccess ? "Payment processed successfully via Kafka event" : 
                                     "Payment failed due to insufficient funds or invalid card";

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .status(status)
                .transactionId(transactionId)
                .paymentMethod("CARD") // Default payment method for event-driven payments
                .remarks(remarks)
                .build();

        payment = paymentRepository.save(payment);

        log.info("💳 Payment {} for order ID: {} with transaction ID: {}", 
                status, event.getOrderId(), transactionId);

        // Publish payment completed event
        PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                .orderId(event.getOrderId())
                .status(status.name())
                .transactionId(transactionId)
                .build();

        paymentProducer.sendPaymentCompletedEvent(completedEvent);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment already refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRemarks("Payment refunded successfully");
        payment = paymentRepository.save(payment);

        log.info("Payment ID: {} refunded successfully", paymentId);
        return mapToResponse(payment);
    }

    /**
     * Simulates payment processing with configurable success rate
     * In a real application, this would integrate with payment gateways like Stripe, PayPal, etc.
     */
    private boolean simulatePayment() {
        // Simulate some processing delay
        try {
            Thread.sleep(random.nextInt(1000) + 500); // 500-1500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Random success/failure based on success rate
        return random.nextDouble() < successRate;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .remarks(payment.getRemarks())
                .timestamp(payment.getTimestamp())
                .build();
    }
}
