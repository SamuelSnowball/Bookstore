package com.bookstore.entity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookstore.entity.models.BookCreateRequest;
import com.bookstore.entity.repository.BookRepository;
import com.example.database.generated.tables.pojos.BookAuthorVw;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void testGetAllBooks() {
        // Arrange
        BookAuthorVw book1 = new BookAuthorVw(
            1, 1, "Book 1", new BigDecimal("19.99"), "Description 1", "First", "Last"
        );

        BookAuthorVw book2 = new BookAuthorVw(
            2, 2, "Book 2", new BigDecimal("29.99"), "Description 2", "First", "Last"
        );

        List<BookAuthorVw> mockBooks = Arrays.asList(book1, book2);
        when(bookRepository.findAll(0)).thenReturn(mockBooks);

        // Act
        List<BookAuthorVw> result = bookService.getAllBooks(0);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("Book 2", result.get(1).getTitle());
        verify(bookRepository, times(1)).findAll(0);
    }

    @Test
    void testGetBookById_Found() {
        // Arrange
        BookAuthorVw book = new BookAuthorVw(
            1, 1, "Test Book", new BigDecimal("19.99"), "Test Description", "First", "Last"
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        // Act
        Optional<BookAuthorVw> result = bookService.getBookById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Book", result.get().getTitle());
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void testGetBookById_NotFound() {
        // Arrange
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<BookAuthorVw> result = bookService.getBookById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(999);
    }

    @Test
    void testCreateBook() {
        // Arrange
        BookCreateRequest newBook = new BookCreateRequest(
            1, "New Book", new BigDecimal("25.00"), "New Description"
        );

        doNothing().when(bookRepository).save(newBook);

        // Act
        bookService.createBook(newBook);

        // Assert
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    void testUpdateBook_Success() {
        // Arrange
        BookAuthorVw existingBook = new BookAuthorVw(
            1, 1, "Old Title", new BigDecimal("19.99"), "Old Description", "First", "Last"
        );

        BookCreateRequest updatedDetails = new BookCreateRequest(
            2, "New Title", new BigDecimal("29.99"), "New Description"
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        doNothing().when(bookRepository).update(existingBook, updatedDetails);

        // Act
        bookService.updateBook(1, updatedDetails);

        // Assert
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).update(existingBook, updatedDetails);
    }

    @Test
    void testUpdateBook_NotFound() {
        // Arrange
        BookCreateRequest updatedDetails = new BookCreateRequest(
            1, "New Title", new BigDecimal("19.99"), "Description"
        );

        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookService.updateBook(999, updatedDetails));
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).update(any(), any());
    }

    @Test
    void testDeleteBook_Success() {
        // Arrange
        BookAuthorVw existingBook = new BookAuthorVw(
            1, 1, "Title", new BigDecimal("19.99"), "Description", "First", "Last"
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        doNothing().when(bookRepository).deleteById(1);

        // Act
        boolean result = bookService.deleteBook(1);

        // Assert
        assertTrue(result);
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteBook_NotFound() {
        // Arrange
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = bookService.deleteBook(999);

        // Assert
        assertFalse(result);
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void testSearchBooks() {
        // Arrange
        BookAuthorVw book1 = new BookAuthorVw(
            1, 1, "Java Programming", new BigDecimal("19.99"), "Description", "First", "Last"
        );

        BookAuthorVw book2 = new BookAuthorVw(
            2, 2, "Advanced Java", new BigDecimal("29.99"), "Description", "First", "Last"
        );

        List<BookAuthorVw> mockBooks = Arrays.asList(book1, book2);
        when(bookRepository.findByTitleContaining("Java")).thenReturn(mockBooks);

        // Act
        List<BookAuthorVw> result = bookService.searchBooks("Java");

        // Assert
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findByTitleContaining("Java");
    }
}
