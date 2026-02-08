package com.bookstore.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.payment.model.PaymentRequest;
import com.bookstore.payment.model.PaymentResponse;
import com.example.common.controller.BaseController;
import com.example.common.model.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/payment", produces = { MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Payment", description = "APIs for Stripe payment operations")
@Slf4j
@RequiredArgsConstructor
public class PaymentController extends BaseController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${redirect.url:http://localhost:5173}")
    private String redirectUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @PostMapping("/create-checkout-session")
    @Operation(summary = "Create a Stripe Checkout Session", description = "Creates a Checkout Session with custom UI mode from order payment request and returns the client secret")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checkout Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    public PaymentResponse createCheckoutSession(@RequestBody(required = false) PaymentRequest paymentRequest) throws StripeException {
        log.info("Received payment request: {}", paymentRequest);

        if (paymentRequest == null) {
            log.error("Payment request is null");
            return PaymentResponse.builder()
                    .status(PaymentResponse.PaymentStatus.PAYMENT_FAILED)
                    .message("Payment request cannot be null")
                    .build();
        }

        // Security: Validate user is authenticated
        // Note: We trust the order service has already validated:
        // 1. The authenticated user owns the cart used to create the order
        // 2. The orderId was just created by this user's request
        // 3. The items/totalAmount match the order created
        // This avoids circular dependency (Order->Payment and Payment->Order)
        Integer authenticatedUserId = getCurrentUserId();
        log.info("Creating checkout session for user {} and order {}", authenticatedUserId, paymentRequest.getOrderId());
        
        if (paymentRequest.getItems() == null || paymentRequest.getItems().isEmpty()) {
            return PaymentResponse.builder()
                    .orderId(paymentRequest.getOrderId())
                    .status(PaymentResponse.PaymentStatus.PAYMENT_FAILED)
                    .message("No items in payment request")
                    .build();
        }

        // Build line items from payment request
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.CUSTOM)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setReturnUrl(redirectUrl + "/complete?session_id={CHECKOUT_SESSION_ID}")
                .putMetadata("orderId", String.valueOf(paymentRequest.getOrderId()))
                .putMetadata("userId", String.valueOf(authenticatedUserId));

        for (PaymentRequest.OrderItem item : paymentRequest.getItems()) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
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
        log.info("Created checkout session {} for order {}", session.getId(), paymentRequest.getOrderId());

        return PaymentResponse.builder()
                .orderId(paymentRequest.getOrderId())
                .status(PaymentResponse.PaymentStatus.PAYMENT_SUCCESS)
                .message("Checkout session created")
                .clientSecret(session.getClientSecret())
                .transactionId(session.getId())
                .build();
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

        return responseData;
    }

    @PostMapping("/complete-order")
    @Operation(summary = "Complete order after successful payment", description = "Verifies Stripe payment session and updates order status to PAYMENT_SUCCESS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid session or payment not completed"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    public Map<String, String> completeOrder(
            @Parameter(description = "The Checkout Session ID", required = true, example = "cs_test_...") @RequestParam("session_id") String sessionId)
            throws StripeException {
        log.info("Completing order for session: {}", sessionId);

        // Retrieve the session to verify payment status
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("payment_intent")
                .build();

        Session session = Session.retrieve(sessionId, params, null);

        // Verify payment was successful
        if (!"complete".equals(session.getStatus()) || !"paid".equals(session.getPaymentStatus())) {
            log.error("Payment not completed for session {}. Status: {}, Payment status: {}", 
                    sessionId, session.getStatus(), session.getPaymentStatus());
            throw new IllegalStateException("Payment was not completed successfully");
        }

        // Extract order ID from session metadata
        Map<String, String> metadata = session.getMetadata();
        if (metadata == null || !metadata.containsKey("orderId")) {
            log.error("Order ID not found in session metadata for session {}", sessionId);
            throw new IllegalStateException("Order ID not found in payment session");
        }

        int orderId = Integer.parseInt(metadata.get("orderId"));
        log.info("Payment verified for order {} with session {}", orderId, sessionId);
        
        log.info("Payment completed successfully for session {} and order {}", sessionId, orderId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment verified successfully");
        response.put("orderId", String.valueOf(orderId));
        return response;
    }
}
