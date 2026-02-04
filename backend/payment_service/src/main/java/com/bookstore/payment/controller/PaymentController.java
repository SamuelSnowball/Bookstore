package com.bookstore.payment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.payment.client.OrderServiceClient;
import com.example.common.controller.BaseController;
import com.example.common.dto.CartItemDetailDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/payment", produces = { MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Payment", description = "APIs for Stripe payment operations")
@Slf4j
public class PaymentController extends BaseController {

    private final OrderServiceClient orderServiceClient;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${redirect.url:http://localhost:5173}")
    private String redirectUrl;

    public PaymentController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @PostMapping("/create-checkout-session")
    @Operation(summary = "Create a Stripe Checkout Session", description = "Creates a Checkout Session with custom UI mode from the authenticated user's cart and returns the client secret")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checkout Session created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"clientSecret\": \"cs_test_...\"}"))),
            @ApiResponse(responseCode = "400", description = "Cart is empty or invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    public Map<String, String> createCheckoutSession() throws StripeException {

        // Fetch cart items for the user via Order Service (JWT token is propagated automatically)
        List<CartItemDetailDto> cartItems = orderServiceClient.getCartItems();

        if (cartItems.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cart is empty");
        }

        // Build line items from cart
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.CUSTOM)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setReturnUrl(redirectUrl + "/complete?session_id={CHECKOUT_SESSION_ID}");

        for (CartItemDetailDto item : cartItems) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getBookQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("gbp")
                                            .setUnitAmount(item.getPrice().movePointRight(2).longValue()) // Convert £10.99 to 1099 pence
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getTitle())
                                                            .build())
                                            .build())
                            .build());
        }

        SessionCreateParams params = paramsBuilder.build();
        Session session = Session.create(params);
        log.info("Created checkout session: {}", session.getId());

        return Map.of("clientSecret", session.getClientSecret());
    }

    /*
     * Justification for public access to /payment/session-status i.e no bearer
     * token:
     * 1. Security is maintained through the use of the Stripe session ID as a
     * secure token
     * 2. Users need to view payment status after being redirected from Stripe
     * 3. No sensitive user data is exposed through this endpoint
     */
    @GetMapping("/session-status")
    @Operation(summary = "Get Checkout Session status", description = "Retrieves the status of a Checkout Session by ID including payment status and intent details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session status retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"status\": \"complete\", \"payment_status\": \"paid\", \"payment_intent_id\": \"pi_...\", \"payment_intent_status\": \"succeeded\"}"))),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    public Map<String, String> getSessionStatus(
            @Parameter(description = "The Checkout Session ID", required = true, example = "cs_test_...") @RequestParam("session_id") String sessionId)
            throws StripeException {
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("payment_intent")
                .build();

        Session session = Session.retrieve(sessionId, params, null);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("status", session.getStatus());
        responseData.put("payment_status", session.getPaymentStatus());

        if (session.getPaymentIntent() != null) {
            responseData.put("payment_intent_id", session.getPaymentIntent());
            // For full payment intent details, you'd need to expand and cast
            // For now, just returning the ID as it's already a string reference
        }

        log.info("Retrieved session status for: {}", sessionId);

        return responseData;
    }

    @PostMapping("/complete-order")
    @Operation(
        summary = "Complete order after successful payment",
        description = "Creates an order from the user's cart after payment is confirmed. Clears the cart after order creation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = "{\"orderId\": 123, \"message\": \"Order created successfully\"}")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Cart is empty or payment not complete"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required"),
        @ApiResponse(responseCode = "500", description = "Error creating order")
    })
    public Map<String, Object> completeOrder(
        @Parameter(description = "The Checkout Session ID", required = true, example = "cs_test_...")
        @RequestParam("session_id") String sessionId) throws StripeException {
        
        // Verify payment was successful
        Session session = Session.retrieve(sessionId, SessionRetrieveParams.builder().build(), null);
        
        if (!"complete".equals(session.getStatus()) || !"paid".equals(session.getPaymentStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Payment not complete"
            );
        }

        // Get authenticated user
        Integer userId = getCurrentUserId();
        
        // Create order from cart via Order Service (JWT token is propagated automatically)
        int orderId = orderServiceClient.createOrderFromCart();
        
        log.info("Completed order {} for user {} after payment {}", orderId, userId, sessionId);
        
        return Map.of(
            "orderId", orderId,
            "message", "Order created successfully"
        );
    }
}
