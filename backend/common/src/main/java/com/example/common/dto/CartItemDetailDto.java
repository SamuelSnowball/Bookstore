package com.example.common.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cart item details - mutable for Jackson serialization/deserialization.
 * Used for service-to-service communication via Feign.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDetailDto {
    private Integer cartItemId;
    private Integer userId;
    private Integer bookId;
    private Integer bookQuantity;
    private Integer authorId;
    private String title;
    private BigDecimal price;
    private String description;
    private String firstName;
    private String lastName;
}
