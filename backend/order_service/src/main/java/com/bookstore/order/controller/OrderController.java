package com.bookstore.order.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.order.service.OrderService;
import com.example.common.controller.BaseController;
import com.example.database.generated.tables.pojos.OrderDetailVw;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/orders", produces = { MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Orders", description = "APIs for order operations")
@RequiredArgsConstructor
@Slf4j
public class OrderController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "Get order history for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = OrderDetailResponse.class)))
            })
    })
    @GetMapping
    public List<OrderDetailResponse> getOrders() {
        Integer userId = getCurrentUserId();
        log.info("Getting orders for user: {}", userId);
        
        List<OrderDetailVw> orderDetails = orderService.getUserOrderDetails(userId);
        
        // Group order details by order ID
        Map<Integer, OrderDetailResponse> orderMap = new LinkedHashMap<>();
        
        for (OrderDetailVw detail : orderDetails) {
            OrderDetailResponse response = orderMap.computeIfAbsent(detail.getId(), id -> {
                OrderDetailResponse newResponse = new OrderDetailResponse();
                newResponse.setOrderId(detail.getId());
                newResponse.setOrderDate(detail.getCreatedAt().toString());
                newResponse.setTotalAmount(detail.getTotalPrice().doubleValue());
                newResponse.setStatus("Completed");
                return newResponse;
            });
            
            // Add book to this order if it exists
            if (detail.getBookId() != null) {
                OrderBookResponse bookResponse = new OrderBookResponse();
                bookResponse.setBookId(detail.getBookId());
                bookResponse.setTitle(detail.getTitle());
                bookResponse.setQuantity(detail.getQuantity());
                bookResponse.setPrice(detail.getPrice().doubleValue());
                response.getBooks().add(bookResponse);
            }
        }
        
        return new ArrayList<>(orderMap.values());
    }

    @Operation(summary = "Create order from cart for authenticated user (called by payment service)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully, returns order ID")
    })
    @PostMapping("/from-cart")
    public int createOrderFromCart() {
        Integer userId = getCurrentUserId();
        log.info("Creating order from cart for user: {}", userId);
        return orderService.createOrderFromCart(userId);
    }

    @Data
    static class OrderDetailResponse {
        private Integer orderId;
        private String orderDate;
        private Double totalAmount;
        private String status;
        private List<OrderBookResponse> books = new ArrayList<>();
    }

    @Data
    static class OrderBookResponse {
        private Integer bookId;
        private String title;
        private Integer quantity;
        private Double price;
    }
}

