package com.bookstore.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.example.database.generated.tables.pojos.OrderDetailVw;
import com.bookstore.order.repository.CartRepository;
import com.bookstore.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final int USER_ID = 1;
    private static final int ORDER_ID = 100;
    private static final int BOOK_ID_1 = 10;
    private static final int BOOK_ID_2 = 20;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemDetailVw cartItem1;

    @Mock
    private CartItemDetailVw cartItem2;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCreateOrderFromCart_Success() {
        // Arrange
        when(cartItem1.getBookId()).thenReturn(BOOK_ID_1);
        when(cartItem1.getBookQuantity()).thenReturn(2);
        when(cartItem1.getPrice()).thenReturn(BigDecimal.valueOf(15.99));
        
        when(cartItem2.getBookId()).thenReturn(BOOK_ID_2);
        when(cartItem2.getBookQuantity()).thenReturn(1);
        when(cartItem2.getPrice()).thenReturn(BigDecimal.valueOf(9.99));
        
        List<CartItemDetailVw> cartItems = Arrays.asList(cartItem1, cartItem2);
        when(cartRepository.getCartItemsByUserId(USER_ID)).thenReturn(cartItems);
        when(orderRepository.createOrder(eq(USER_ID), any(BigDecimal.class))).thenReturn(ORDER_ID);

        // Act
        int result = orderService.createOrderFromCart(USER_ID);

        // Assert
        assertEquals(ORDER_ID, result);
        verify(orderRepository, times(1)).createOrder(eq(USER_ID), any(BigDecimal.class));
        verify(orderRepository, times(1)).addBookToOrder(ORDER_ID, BOOK_ID_1, BigDecimal.valueOf(15.99), 2);
        verify(orderRepository, times(1)).addBookToOrder(ORDER_ID, BOOK_ID_2, BigDecimal.valueOf(9.99), 1);
        verify(cartRepository, times(1)).clearCart(USER_ID);
    }

    @Test
    void testCreateOrderFromCart_EmptyCart_ThrowsException() {
        // Arrange
        when(cartRepository.getCartItemsByUserId(USER_ID)).thenReturn(new ArrayList<>());

        // Act / Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.createOrderFromCart(USER_ID);
        });
        
        verify(orderRepository, never()).createOrder(anyInt(), any(BigDecimal.class));
        verify(cartRepository, never()).clearCart(anyInt());
    }

    @Test
    void testCreateOrderFromCart_CalculatesTotalCorrectly() {
        // Arrange - cart with specific values to test calculation
        when(cartItem1.getBookId()).thenReturn(BOOK_ID_1);
        when(cartItem1.getBookQuantity()).thenReturn(3);
        when(cartItem1.getPrice()).thenReturn(BigDecimal.valueOf(10.00));
        
        when(cartItem2.getBookId()).thenReturn(BOOK_ID_2);
        when(cartItem2.getBookQuantity()).thenReturn(2);
        when(cartItem2.getPrice()).thenReturn(BigDecimal.valueOf(25.50));
        
        List<CartItemDetailVw> cartItems = Arrays.asList(cartItem1, cartItem2);
        when(cartRepository.getCartItemsByUserId(USER_ID)).thenReturn(cartItems);
        when(orderRepository.createOrder(eq(USER_ID), any(BigDecimal.class))).thenReturn(ORDER_ID);

        // Act
        orderService.createOrderFromCart(USER_ID);

        // Assert - verify total is (3 * 10.00) + (2 * 25.50) = 30.00 + 51.00 = 81.00
        verify(orderRepository).createOrder(eq(USER_ID), eq(BigDecimal.valueOf(81.0)));
    }

    @Test
    void testGetUserOrderDetails() {
        // Arrange
        OrderDetailVw order1 = new OrderDetailVw(
            ORDER_ID,
            USER_ID,
            BigDecimal.valueOf(29.99),
            LocalDateTime.now(),
            10,
            "Book Title 1",
            BigDecimal.valueOf(29.99),
            1
        );
        
        OrderDetailVw order2 = new OrderDetailVw(
            ORDER_ID + 1,
            USER_ID,
            BigDecimal.valueOf(49.99),
            LocalDateTime.now(),
            20,
            "Book Title 2",
            BigDecimal.valueOf(49.99),
            1
        );
        
        List<OrderDetailVw> expectedOrders = Arrays.asList(order1, order2);
        when(orderRepository.getOrderDetailsByUserId(USER_ID)).thenReturn(expectedOrders);

        // Act
        List<OrderDetailVw> result = orderService.getUserOrderDetails(USER_ID);

        // Assert
        assertEquals(2, result.size());
        assertEquals(ORDER_ID, result.get(0).getId());
        assertEquals(ORDER_ID + 1, result.get(1).getId());
        verify(orderRepository, times(1)).getOrderDetailsByUserId(USER_ID);
    }

    @Test
    void testGetUserOrderDetails_EmptyList() {
        // Arrange
        when(orderRepository.getOrderDetailsByUserId(USER_ID)).thenReturn(new ArrayList<>());

        // Act
        List<OrderDetailVw> result = orderService.getUserOrderDetails(USER_ID);

        // Assert
        assertEquals(0, result.size());
        verify(orderRepository, times(1)).getOrderDetailsByUserId(USER_ID);
    }
}
