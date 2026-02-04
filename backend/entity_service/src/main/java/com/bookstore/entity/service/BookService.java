package com.bookstore.entity.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.entity.models.BookCreateRequest;
import com.bookstore.entity.repository.BookRepository;
import com.example.database.generated.tables.pojos.BookAuthorVw;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookAuthorVw> getAllBooks(Integer prevPageLastBookId) {
        return bookRepository.findAll(prevPageLastBookId);
    }

    public Optional<BookAuthorVw> getBookById(Integer id) {
        return bookRepository.findById(id);
    }

    public void createBook(BookCreateRequest book) {
        bookRepository.save(book);
    }

    public void updateBook(Integer id, BookCreateRequest bookDetails) {
        BookAuthorVw existing = bookRepository.findById(id).orElse(null);

        if (existing != null) {
            bookRepository.update(existing, bookDetails);
        } else {
            throw new RuntimeException("Book not found with id: " + id);
        }
    }

    public boolean deleteBook(Integer id) {
        return bookRepository.findById(id)
                .map(book -> {
                    bookRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    public List<BookAuthorVw> searchBooks(String title) {
        return bookRepository.findByTitleContaining(title);
    }
}
