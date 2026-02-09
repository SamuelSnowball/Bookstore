package com.bookstore.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bookstore.order.model.PaymentRequest;
import com.bookstore.order.model.PaymentResponse;
import com.example.common.config.FeignConfig;

/**
 * Feign client for calling Payment Service APIs.
 * FeignConfig (from common module) propagates the JWT token from the incoming request.
 */
@FeignClient(name = "payment-service", url = "${payment.service.url:http://localhost:9003}", configuration = FeignConfig.class)
public interface PaymentServiceClient {
    
    @PostMapping("/payment/create-checkout-session")
    PaymentResponse createCheckoutSession(@RequestBody PaymentRequest paymentRequest);
}
