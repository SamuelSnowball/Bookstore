package com.bookstore.order.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.example.common.security.CustomJwtDecoder;
import com.example.common.security.JwtUtil;
import com.bookstore.order.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.integration.spring.SpringLiquibase;

@WebMvcTest(controllers = CartController.class)
class CartControllerTest {

    private static final int USER_ID = 1;
    private static final int BOOK_ID = 100;
    private static final int CART_ITEM_ID = 1;
    private static final int QUANTITY = 3;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

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

    @MockitoBean
    private CartItemDetailVw cartItem;

    @Test
    void testGetCartItems_Returns200() throws Exception {
        // Setup
        List<CartItemDetailVw> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        final String expectedResponseContent = objectMapper.writeValueAsString(cartItems);
        when(cartService.getCartItems(USER_ID)).thenReturn(cartItems);
        // Act / Assert
        mockMvc.perform(get("/cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseContent));
        verify(cartService, times(1)).getCartItems(USER_ID);
    }

    @Test
    void testAddToCart_Returns201() throws Exception {
        // Setup
        String requestBody = String.format(
                "{\"bookId\":%d,\"quantity\":%d}",
                BOOK_ID, QUANTITY);
        // Act / Assert
        mockMvc.perform(post("/cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated());
        verify(cartService, times(1)).addToCart(USER_ID, BOOK_ID, QUANTITY);
    }

    @Test
    void testAddToCart_DefaultQuantity_Returns201() throws Exception {
        // Setup - request without quantity field should default to 1
        String requestBody = String.format(
                "{\"bookId\":%d}",
                BOOK_ID);
        // Act / Assert
        mockMvc.perform(post("/cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated());
        verify(cartService, times(1)).addToCart(USER_ID, BOOK_ID, 1);
    }

    @Test
    void testUpdateQuantity_Returns200() throws Exception {
        // Setup
        String requestBody = String.format("{\"quantity\":%d}", QUANTITY);
        // Act / Assert
        mockMvc.perform(put("/cart/" + CART_ITEM_ID)
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk());
        verify(cartService, times(1)).updateQuantity(USER_ID, CART_ITEM_ID, QUANTITY);
    }

    @Test
    void testRemoveItem_Returns204() throws Exception {
        // Act / Assert
        mockMvc.perform(delete("/cart/" + CART_ITEM_ID)
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
            .andExpect(status().isNoContent());
        verify(cartService, times(1)).removeItem(USER_ID, CART_ITEM_ID);
    }

    @Test
    void testClearCart_Returns204() throws Exception {
        // Act / Assert
        mockMvc.perform(delete("/cart")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
            .andExpect(status().isNoContent());
        verify(cartService, times(1)).clearCart(USER_ID);
    }
}

