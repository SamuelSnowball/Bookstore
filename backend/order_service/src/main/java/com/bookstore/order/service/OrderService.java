package com.bookstore.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.order.client.PaymentServiceClient;
import com.bookstore.order.model.PaymentRequest;
import com.bookstore.order.model.PaymentResponse;
import com.example.common.model.OrderStatus;
import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.example.database.generated.tables.pojos.OrderDetailVw;
import com.bookstore.order.repository.CartRepository;
import com.bookstore.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Creates an order from the user's cart with status CREATED.
     * After creation, triggers payment processing asynchronously.
     */
    @Transactional
    public int createOrderFromCart(int userId) {
        // Get cart items
        List<CartItemDetailVw> cartItems = cartRepository.getCartItemsByUserId(userId);

        if (cartItems.isEmpty()) {
            log.warn("Attempted to create order from empty cart for user with ID {}", userId);
            throw new IllegalStateException("Cart is empty");
        }

        BigDecimal totalPrice = BigDecimal.valueOf(cartItems.stream()
                .mapToDouble(item -> item.getBookQuantity() * item.getPrice().doubleValue())
                .sum());

        // Create order with CREATED status
        int orderId = orderRepository.createOrder(userId, totalPrice, OrderStatus.CREATED);

        log.info("Created order {} from cart for user with ID {} with status CREATED", orderId, userId);

        // Add books to order
        for (CartItemDetailVw item : cartItems) {
            orderRepository.addBookToOrder(
                    orderId,
                    item.getBookId(),
                    item.getPrice(),
                    item.getBookQuantity());
            log.debug("Added book {} (quantity {}) to order {}", item.getBookId(), item.getBookQuantity(), orderId);
        }

        // Clear the cart
        cartRepository.clearCart(userId);
        log.debug("Cleared cart for user with ID {}", userId);

        // Prepare payment request
        PaymentRequest paymentRequest = buildPaymentRequest(userId, orderId, totalPrice, cartItems);

        // Process payment asynchronously with security context propagation
        processPaymentAsync(paymentRequest, orderId);

        return orderId;
    }

    /**
     * Process payment asynchronously while propagating SecurityContext
     */
    private void processPaymentAsync(PaymentRequest paymentRequest, int orderId) {
        // Capture the current security context BEFORE going async
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        log.info("Starting async payment processing for order {}", orderId);

        CompletableFuture.supplyAsync(() -> {
            try {
                // Restore security context in the new thread
                SecurityContextHolder.setContext(securityContext);
                
                // Call payment service via OpenFeign
                PaymentResponse response = paymentServiceClient.createCheckoutSession(paymentRequest);

                // Update order status based on payment result
                OrderStatus newStatus = response.getStatus() == PaymentResponse.PaymentStatus.PAYMENT_SUCCESS
                        ? OrderStatus.PAYMENT_SUCCESS
                        : OrderStatus.PAYMENT_FAILED;

                updateOrderStatus(orderId, newStatus);
                log.info("Payment processing completed for order {} with status {}", orderId, newStatus);

                return response;
            } catch (Exception e) {
                log.error("Payment processing failed for order {}", orderId, e);
                updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED);
                throw e;
            } finally {
                // Clear security context from this thread
                SecurityContextHolder.clearContext();
            }
        }).thenRun(() -> {
            log.info("Async payment processing task completed for order {}", orderId);
        });
    }

    /**
     * Builds payment request from cart items
     */
    private PaymentRequest buildPaymentRequest(int userId, int orderId, BigDecimal totalPrice,
            List<CartItemDetailVw> cartItems) {
        List<PaymentRequest.OrderItem> items = cartItems.stream()
                .map(item -> PaymentRequest.OrderItem.builder()
                        .bookId(item.getBookId())
                        .title(item.getTitle())
                        .price(item.getPrice())
                        .quantity(item.getBookQuantity())
                        .build())
                .collect(Collectors.toList());

        return PaymentRequest.builder()
                .userId(userId)
                .orderId(orderId)
                .totalAmount(totalPrice)
                .items(items)
                .build();
    }

    /**
     * Updates the status of an order
     */
    @Transactional
    public void updateOrderStatus(int orderId, OrderStatus status) {
        orderRepository.updateOrderStatus(orderId, status);
        log.info("Order {} status updated to {}", orderId, status);
    }

    public List<OrderDetailVw> getUserOrderDetails(int userId) {
        return orderRepository.getOrderDetailsByUserId(userId);
    }
}
