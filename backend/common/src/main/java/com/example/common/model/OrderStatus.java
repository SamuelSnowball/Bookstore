package com.example.common.model;

/**
 * Represents the lifecycle states of an order
 */
public enum OrderStatus {
    /** Order has been created, awaiting payment */
    CREATED,
    
    /** Payment is being processed */
    PAYMENT_PROCESSING,
    
    /** Payment was successful */
    PAYMENT_SUCCESS,
    
    /** Payment failed */
    PAYMENT_FAILED,
    
    /** Order has been completed and fulfilled */
    COMPLETED,
    
    /** Order was cancelled */
    CANCELLED
}
