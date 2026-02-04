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

import com.bookstore.entity.models.AuthorCreateRequest;
import com.bookstore.entity.service.AuthorService;
import com.example.common.controller.BaseController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/author")
@Tag(name = "Author", description = "Author management APIs")
@RequiredArgsConstructor
public class AuthorController extends BaseController {

    private final AuthorService authorService;

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieve a list of all authors")
    public ResponseEntity<List<com.example.database.generated.tables.pojos.Author>> getAllAuthors() {
        List<com.example.database.generated.tables.pojos.Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by their ID")
    public ResponseEntity<com.example.database.generated.tables.pojos.Author> getAuthorById(@PathVariable Integer id) {
        return authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new author", description = "Add a new author to the database")
    public ResponseEntity<Void> createAuthor(@RequestBody AuthorCreateRequest author) {
        authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an author", description = "Update an existing author's information")
    public ResponseEntity<Void> updateAuthor(@PathVariable Integer id, @RequestBody AuthorCreateRequest authorDetails) {
        try {
            authorService.updateAuthor(id, authorDetails);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an author", description = "Remove an author from the database")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        if (authorService.deleteAuthor(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search authors", description = "Search for authors by last name")
    public ResponseEntity<List<com.example.database.generated.tables.pojos.Author>> searchAuthors(@RequestParam String lastName) {
        List<com.example.database.generated.tables.pojos.Author> authors = authorService.searchAuthors(lastName);
        return ResponseEntity.ok(authors);
    }
}
