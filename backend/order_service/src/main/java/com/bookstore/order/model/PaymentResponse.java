package com.bookstore.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from payment service after processing payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer orderId;
    private PaymentStatus status;
    private String message;
    private String transactionId;

    public enum PaymentStatus {
        PAYMENT_SUCCESS,
        PAYMENT_FAILED
    }
}
