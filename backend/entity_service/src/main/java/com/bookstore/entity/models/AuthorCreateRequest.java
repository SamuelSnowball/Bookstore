package com.bookstore.entity.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AuthorCreateRequest {
    private final String firstName;
    private final String lastName;
}
