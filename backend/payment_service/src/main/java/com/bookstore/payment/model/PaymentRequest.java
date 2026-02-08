package com.bookstore.payment.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request to payment service to process a payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentRequest {
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("orderId")
    private Integer orderId;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("items")
    private List<OrderItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class OrderItem {
        @JsonProperty("bookId")
        private Integer bookId;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("price")
        private BigDecimal price;
        
        @JsonProperty("quantity")
        private Integer quantity;
    }
}
