package com.bookstore.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookstore.payment.client.OrderServiceClient;
import com.example.common.dto.CartItemDetailDto;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Tests for PaymentController using mocked Stripe API.
 * Note: Stripe static API calls are difficult to mock, so we're testing the REST layer.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceClient orderServiceClient;

    @Test
    void createCheckoutSession_WithEmptyCart_ReturnsBadRequest() throws Exception {
        // Given: User has empty cart
        when(orderServiceClient.getCartItems()).thenReturn(List.of());

        // When/Then: Creating checkout session fails
        mockMvc.perform(post("/payment/create-checkout-session")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderServiceClient).getCartItems();
    }

    @Test
    void createCheckoutSession_WithoutAuth_ReturnsUnauthorized() throws Exception {
        // When/Then: Request without token is rejected
        mockMvc.perform(post("/payment/create-checkout-session")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completeOrder_WithoutAuth_ReturnsUnauthorized() throws Exception {
        // When/Then: Request without token is rejected
        mockMvc.perform(post("/payment/complete-order")
                .param("session_id", "cs_test_123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCheckoutSession_WithValidCart_CallsStripeWithCorrectParams() throws Exception {
        // Given: User has items in cart
        List<CartItemDetailDto> cartItems = List.of(
            new CartItemDetailDto(1, 1, 101, 2, 1, "The Great Gatsby", 
                new BigDecimal("10.99"), "Classic novel", "F. Scott", "Fitzgerald"),
            new CartItemDetailDto(2, 1, 102, 1, 2, "1984", 
                new BigDecimal("8.50"), "Dystopian fiction", "George", "Orwell")
        );
        
        when(orderServiceClient.getCartItems()).thenReturn(cartItems);

        // Mock Stripe Session.create static method
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            Session mockSession = org.mockito.Mockito.mock(Session.class);
            when(mockSession.getId()).thenReturn("cs_test_12345");
            when(mockSession.getClientSecret()).thenReturn("cs_test_12345_secret_xyz");
            
            ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
            sessionMock.when(() -> Session.create(paramsCaptor.capture())).thenReturn(mockSession);

            // When: Creating checkout session
            mockMvc.perform(post("/payment/create-checkout-session")
                    .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clientSecret").value("cs_test_12345_secret_xyz"));

            // Then: Verify Session.create was called with correct parameters
            SessionCreateParams capturedParams = paramsCaptor.getValue();
            
            assertEquals(SessionCreateParams.UiMode.CUSTOM, capturedParams.getUiMode());
            assertEquals(SessionCreateParams.Mode.PAYMENT, capturedParams.getMode());
            assertEquals("http://localhost:5173/complete?session_id={CHECKOUT_SESSION_ID}", 
                capturedParams.getReturnUrl());
            
            // Verify line items
            List<SessionCreateParams.LineItem> lineItems = capturedParams.getLineItems();
            assertEquals(2, lineItems.size());
            
            // First item: The Great Gatsby, qty 2, £10.99
            SessionCreateParams.LineItem item1 = lineItems.get(0);
            assertEquals(2L, item1.getQuantity());
            assertEquals("gbp", item1.getPriceData().getCurrency());
            assertEquals(1099L, item1.getPriceData().getUnitAmount()); // £10.99 = 1099 pence
            assertEquals("The Great Gatsby", item1.getPriceData().getProductData().getName());
            
            // Second item: 1984, qty 1, £8.50
            SessionCreateParams.LineItem item2 = lineItems.get(1);
            assertEquals(1L, item2.getQuantity());
            assertEquals("gbp", item2.getPriceData().getCurrency());
            assertEquals(850L, item2.getPriceData().getUnitAmount()); // £8.50 = 850 pence
            assertEquals("1984", item2.getPriceData().getProductData().getName());

            verify(orderServiceClient).getCartItems();
        }
    }
}
