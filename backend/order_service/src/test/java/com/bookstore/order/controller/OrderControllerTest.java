package com.bookstore.order.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.common.security.CustomJwtDecoder;
import com.example.common.security.JwtUtil;
import com.example.database.generated.tables.pojos.OrderDetailVw;
import com.bookstore.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.integration.spring.SpringLiquibase;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    private static final int USER_ID = 1;
    private static final int ORDER_ID = 100;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;
    
    @MockitoBean
    private com.example.common.repository.UserRepository userRepository;
    
    @MockitoBean
    private DataSource dataSource;
    
    @MockitoBean
    private SpringLiquibase liquibase;

    @Test
    void testGetOrders_Returns200() throws Exception {
        // Setup - create mock order details
        OrderDetailVw orderDetail = new OrderDetailVw(
            ORDER_ID,
            USER_ID,
            BigDecimal.valueOf(29.99),
            LocalDateTime.of(2026, 1, 15, 10, 30),
            10,
            "Test Book Title",
            BigDecimal.valueOf(29.99),
            1
        );
        
        List<OrderDetailVw> orderDetails = new ArrayList<>();
        orderDetails.add(orderDetail);
        
        when(orderService.getUserOrderDetails(USER_ID)).thenReturn(orderDetails);
        
        // Act / Assert
        mockMvc.perform(get("/orders")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(ORDER_ID))
                .andExpect(jsonPath("$[0].totalAmount").value(29.99))
                .andExpect(jsonPath("$[0].status").value("Completed"))
                .andExpect(jsonPath("$[0].books[0].bookId").value(10))
                .andExpect(jsonPath("$[0].books[0].title").value("Test Book Title"));
        
        verify(orderService, times(1)).getUserOrderDetails(USER_ID);
    }

    @Test
    void testGetOrders_EmptyList_Returns200() throws Exception {
        // Setup - empty orders
        when(orderService.getUserOrderDetails(USER_ID)).thenReturn(new ArrayList<>());
        
        // Act / Assert
        mockMvc.perform(get("/orders")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        
        verify(orderService, times(1)).getUserOrderDetails(USER_ID);
    }

    @Test
    void testCreateOrderFromCart_Success() throws Exception {
        // Setup
        int createdOrderId = 200;
        when(orderService.createOrderFromCart(USER_ID)).thenReturn(createdOrderId);
        
        // Act / Assert - Simulating payment service calling with JWT token
        mockMvc.perform(post("/api/orders/from-cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(createdOrderId)));
        
        verify(orderService, times(1)).createOrderFromCart(USER_ID);
    }

    @Test
    void testCreateOrderFromCart_EmptyCart_Returns500() throws Exception {
        // Setup - simulate empty cart exception
        when(orderService.createOrderFromCart(USER_ID))
                .thenThrow(new IllegalStateException("Cart is empty"));
        
        // Act / Assert - Simulating payment service calling with JWT token
        mockMvc.perform(post("/api/orders/from-cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isInternalServerError());
        
        verify(orderService, times(1)).createOrderFromCart(USER_ID);
    }

    @Test
    void testCreateOrderFromCart_DifferentUserId() throws Exception {
        // Setup
        int differentUserId = 999;
        int createdOrderId = 300;
        when(orderService.createOrderFromCart(differentUserId)).thenReturn(createdOrderId);
        
        // Act / Assert - Simulating payment service calling with JWT token for different user
        mockMvc.perform(post("/api/orders/from-cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", differentUserId)))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(createdOrderId)));
        
        verify(orderService, times(1)).createOrderFromCart(differentUserId);
    }

    @Test
    void testCreateOrderFromCart_Unauthorized_Returns401() throws Exception {
        // Act / Assert - Calling without JWT token should return 401
        mockMvc.perform(post("/api/orders/from-cart")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(orderService, times(0)).createOrderFromCart(anyInt());
    }
}
