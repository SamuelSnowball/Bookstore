package com.bookstore.payment.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.common.config.FeignConfig;
import com.example.common.dto.CartItemDetailDto;

/**
 * Feign client for calling Order Service APIs.
 * Uses service name "order-service" for service discovery.
 * URL can be configured via application.properties if not using service discovery.
 * FeignConfig (from common module) propagates the JWT token from the incoming request.
 */
@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:9002}", configuration = FeignConfig.class)
public interface OrderServiceClient {
    
    @GetMapping("/cart")
    List<CartItemDetailDto> getCartItems();
    
    @PostMapping("/orders/from-cart")
    int createOrderFromCart();
}
