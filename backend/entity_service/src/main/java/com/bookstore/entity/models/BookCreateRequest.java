package com.bookstore.entity.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor   
public class BookCreateRequest {
    private final Integer authorId;
    private final String title;
    private final java.math.BigDecimal price;
    private final String description;
}
