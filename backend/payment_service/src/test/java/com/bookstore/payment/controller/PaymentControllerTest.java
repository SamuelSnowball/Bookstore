package com.bookstore.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookstore.payment.model.PaymentRequest;
import com.example.common.database.MyDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

/**
 * Tests for PaymentController using mocked Stripe API.
 * Note: Stripe static API calls are difficult to mock, so we're testing the REST layer.
 */
@WebMvcTest(
    controllers = PaymentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MyDataSource.class})
)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCheckoutSession_WithEmptyCart_ReturnsPaymentFailed() throws Exception {
        // Given: Payment request with empty items
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .userId(1)
                .orderId(100)
                .totalAmount(new BigDecimal("0.00"))
                .items(List.of())
                .build();

        // When/Then: Creating checkout session returns payment failed
        mockMvc.perform(post("/payment/create-checkout-session")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.message").value("No items in payment request"))
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void createCheckoutSession_WithNullItems_ReturnsPaymentFailed() throws Exception {
        // Given: Payment request with null items
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .userId(1)
                .orderId(101)
                .totalAmount(new BigDecimal("0.00"))
                .items(null)
                .build();

        // When/Then: Creating checkout session returns payment failed
        mockMvc.perform(post("/payment/create-checkout-session")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.message").value("No items in payment request"))
                .andExpect(jsonPath("$.orderId").value(101));
    }

    @Test
    void sessionStatus_RetrievesSessionInfo() throws Exception {
        // Given: Mock Stripe session
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            Session mockSession = org.mockito.Mockito.mock(Session.class);
            when(mockSession.getStatus()).thenReturn("complete");
            when(mockSession.getPaymentStatus()).thenReturn("paid");
            when(mockSession.getPaymentIntent()).thenReturn("pi_test_12345");
            
            sessionMock.when(() -> Session.retrieve(
                    org.mockito.ArgumentMatchers.eq("cs_test_123"),
                    org.mockito.ArgumentMatchers.any(SessionRetrieveParams.class),
                    org.mockito.ArgumentMatchers.any()
            )).thenReturn(mockSession);

            // When/Then: Can retrieve session status
            mockMvc.perform(get("/payment/session-status")
                    .param("session_id", "cs_test_123")
                    .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("complete"))
                    .andExpect(jsonPath("$.payment_status").value("paid"))
                    .andExpect(jsonPath("$.payment_intent_id").value("pi_test_12345"));
        }
    }

    @Test
    void createCheckoutSession_WithValidCart_CallsStripeWithCorrectParams() throws Exception {
        // Given: Payment request with items
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .userId(1)
                .orderId(100)
                .totalAmount(new BigDecimal("30.48"))
                .items(List.of(
                        PaymentRequest.OrderItem.builder()
                                .bookId(101)
                                .title("The Great Gatsby")
                                .price(new BigDecimal("10.99"))
                                .quantity(2)
                                .build(),
                        PaymentRequest.OrderItem.builder()
                                .bookId(102)
                                .title("1984")
                                .price(new BigDecimal("8.50"))
                                .quantity(1)
                                .build()
                ))
                .build();

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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAYMENT_SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Checkout session created"))
                    .andExpect(jsonPath("$.clientSecret").value("cs_test_12345_secret_xyz"))
                    .andExpect(jsonPath("$.transactionId").value("cs_test_12345"))
                    .andExpect(jsonPath("$.orderId").value(100));

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
        }
    }
}
