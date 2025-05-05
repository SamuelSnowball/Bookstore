package com.example.controller;

import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.database.generatedclasses.tables.pojos.Book;
import com.example.service.BookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;

    @GetMapping
	public List<? extends Book> getBooks(@RequestParam(required=false) boolean includeAuthors) {
		return bookService.getBooks(includeAuthors);
	}
	
    // Use Validated to ensure authorId is a int, currently use Integer.parseInt within the service
	@PostMapping
	public Book insert(
		@RequestBody Map<String, String> body) {
		return bookService.insert(body.get("authorId"), body.get("title"));
	}
}