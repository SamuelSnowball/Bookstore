package com.bookstore.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.example.database.generated.tables.pojos.OrderDetailVw;
import com.bookstore.order.repository.CartRepository;
import com.bookstore.order.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Creates an order from the user's cart and clears the cart.
     * This should be called after successful payment.
     */
    @Transactional
    public int createOrderFromCart(int userId) {
        // Get cart items
        List<CartItemDetailVw> cartItems = cartRepository.getCartItemsByUserId(userId);
        
        if (cartItems.isEmpty()) {
            log.warn("Attempted to create order from empty cart for user with ID {}", userId);
            throw new IllegalStateException("Cart is empty");
        }

        BigDecimal totalPrice = BigDecimal.valueOf(cartItems.stream().mapToDouble(item -> item.getBookQuantity() * item.getPrice().doubleValue())
                .sum());

        // Create order and get the generated ID
        int orderId = orderRepository.createOrder(userId, totalPrice);

        log.info("Created order {} from cart for user with ID {}", orderId, userId);

        // Add books to order
        for (CartItemDetailVw item : cartItems) {
            orderRepository.addBookToOrder(
                    orderId,
                    item.getBookId(),
                    item.getPrice(),
                    item.getBookQuantity()
            );
            log.debug("Added book {} (quantity {}) to order {}", item.getBookId(), item.getBookQuantity(), orderId);
        }

        // Clear the cart
        cartRepository.clearCart(userId);
        
        log.debug("Cleared cart for user with ID {}", userId);

        return orderId;
    }

    public List<OrderDetailVw> getUserOrderDetails(int userId) {
        return orderRepository.getOrderDetailsByUserId(userId);
    }
}

