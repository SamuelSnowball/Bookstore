package com.example.models;

import com.example.database.generatedclasses.tables.pojos.Book;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// Think I also need noArgs for jooq
public class BookWithAuthor extends Book {
    private final String firstName;
    private final String lastName;
}
