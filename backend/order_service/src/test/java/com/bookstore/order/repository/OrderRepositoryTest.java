package com.bookstore.order.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.common.repository.BaseIntegrationTest;
import com.example.database.generated.tables.pojos.OrderDetailVw;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyDataSource.class, OrderRepository.class})
class OrderRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private static final int USER_ID = 1;
    private static final int BOOK_ID_1 = 1;
    private static final int BOOK_ID_2 = 2;
    private static final BigDecimal TOTAL_PRICE = BigDecimal.valueOf(59.99);
    private static final BigDecimal BOOK_PRICE_1 = BigDecimal.valueOf(29.99);
    private static final BigDecimal BOOK_PRICE_2 = BigDecimal.valueOf(30.00);

    @Test
    void testCreateOrder() {
        // Act
        int orderId = orderRepository.createOrder(USER_ID, TOTAL_PRICE);

        // Assert
        assertNotNull(orderId);
        assertTrue(orderId > 0);
        
        // Verify order was created
        List<OrderDetailVw> orderDetails = orderRepository.getOrderDetailsByUserId(USER_ID);
        assertTrue(orderDetails.stream().anyMatch(o -> o.getId().equals(orderId)));
    }

    @Test
    void testAddBookToOrder() {
        // Arrange - create an order first
        int orderId = orderRepository.createOrder(USER_ID, TOTAL_PRICE);

        // Act - add books to the order
        orderRepository.addBookToOrder(orderId, BOOK_ID_1, BOOK_PRICE_1, 2);
        orderRepository.addBookToOrder(orderId, BOOK_ID_2, BOOK_PRICE_2, 1);

        // Assert - verify using OrderDetailVw
        List<OrderDetailVw> orderDetails = orderRepository.getOrderDetailsByUserId(USER_ID);
        List<OrderDetailVw> thisOrderDetails = orderDetails.stream()
            .filter(od -> od.getId().equals(orderId))
            .toList();
        assertEquals(2, thisOrderDetails.size());
        
        // Verify first book
        OrderDetailVw bookDetail1 = thisOrderDetails.stream()
            .filter(od -> od.getBookId().equals(BOOK_ID_1))
            .findFirst()
            .orElse(null);
        assertNotNull(bookDetail1);
        assertEquals(orderId, bookDetail1.getId());
        assertEquals(2, bookDetail1.getQuantity());
        assertEquals(BOOK_PRICE_1, bookDetail1.getPrice());
        
        // Verify second book
        OrderDetailVw bookDetail2 = thisOrderDetails.stream()
            .filter(od -> od.getBookId().equals(BOOK_ID_2))
            .findFirst()
            .orElse(null);
        assertNotNull(bookDetail2);
        assertEquals(orderId, bookDetail2.getId());
        assertEquals(1, bookDetail2.getQuantity());
        assertEquals(0, BOOK_PRICE_2.compareTo(bookDetail2.getPrice()));
    }

    @Test
    void testGetOrderDetailsByUserId() {
        // Arrange - create multiple orders
        int orderId1 = orderRepository.createOrder(USER_ID, BigDecimal.valueOf(29.99));
        int orderId2 = orderRepository.createOrder(USER_ID, BigDecimal.valueOf(49.99));

        // Act
        List<OrderDetailVw> orderDetails = orderRepository.getOrderDetailsByUserId(USER_ID);

        // Assert
        assertTrue(orderDetails.size() >= 2);
        assertTrue(orderDetails.stream().anyMatch(o -> o.getId().equals(orderId1)));
        assertTrue(orderDetails.stream().anyMatch(o -> o.getId().equals(orderId2)));
        
        // Verify orders are sorted by created_at descending (most recent first)
        OrderDetailVw firstOrder = orderDetails.get(0);
        OrderDetailVw secondOrder = orderDetails.get(1);
        assertTrue(firstOrder.getCreatedAt().isAfter(secondOrder.getCreatedAt()) 
                || firstOrder.getCreatedAt().isEqual(secondOrder.getCreatedAt()));
    }

    @Test
    void testGetOrderDetailsByUserId_EmptyList() {
        // Act - use a user ID that has no orders
        List<OrderDetailVw> orderDetails = orderRepository.getOrderDetailsByUserId(999);

        // Assert
        assertEquals(0, orderDetails.size());
    }

    @Test
    void testCompleteOrderFlow() {
        // This test simulates a complete order creation flow
        
        // Step 1: Create order
        int orderId = orderRepository.createOrder(USER_ID, TOTAL_PRICE);
        assertNotNull(orderId);
        
        // Step 2: Add books to order
        orderRepository.addBookToOrder(orderId, BOOK_ID_1, BOOK_PRICE_1, 2);
        orderRepository.addBookToOrder(orderId, BOOK_ID_2, BOOK_PRICE_2, 1);
        
        // Step 3: Verify order exists in user's order details
        List<OrderDetailVw> userOrderDetails = orderRepository.getOrderDetailsByUserId(USER_ID);
        List<OrderDetailVw> thisOrderDetails = userOrderDetails.stream()
            .filter(od -> od.getId().equals(orderId))
            .toList();
        assertNotNull(thisOrderDetails);
        assertTrue(thisOrderDetails.size() > 0);
        assertEquals(TOTAL_PRICE, thisOrderDetails.get(0).getTotalPrice());
        
        // Step 4: Verify books in order
        assertEquals(2, thisOrderDetails.size());
        
        // Verify total quantity
        int totalQuantity = thisOrderDetails.stream()
            .mapToInt(OrderDetailVw::getQuantity)
            .sum();
        assertEquals(3, totalQuantity);
    }
}
