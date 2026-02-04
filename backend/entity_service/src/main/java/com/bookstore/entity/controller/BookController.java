package com.bookstore.entity.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.entity.models.BookCreateRequest;
import com.bookstore.entity.service.BookService;
import com.example.common.controller.BaseController;
import com.example.database.generated.tables.pojos.BookAuthorVw;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/book")
@Tag(name = "Book", description = "Book management APIs")
@RequiredArgsConstructor
public class BookController extends BaseController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve a list of all books")
    public ResponseEntity<List<BookAuthorVw>> getAllBooks(@RequestParam(required = false, defaultValue = "0") Integer prevPageLastBookId) {
        List<BookAuthorVw> books = bookService.getAllBooks(prevPageLastBookId);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    public ResponseEntity<BookAuthorVw> getBookById(@PathVariable Integer id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new book", description = "Add a new book to the catalog")
    public ResponseEntity<Void> createBook(@RequestBody BookCreateRequest book) {
        bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Update an existing book's information")
    public ResponseEntity<Void> updateBook(@PathVariable Integer id, @RequestBody BookCreateRequest bookDetails) {
        try {
            bookService.updateBook(id, bookDetails);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Remove a book from the catalog")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search for books by title")
    public ResponseEntity<List<BookAuthorVw>> searchBooks(@RequestParam String title) {
        List<BookAuthorVw> books = bookService.searchBooks(title);
        return ResponseEntity.ok(books);
    }
}
