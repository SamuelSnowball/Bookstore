package com.bookstore.entity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookstore.entity.models.AuthorCreateRequest;
import com.bookstore.entity.repository.AuthorRepository;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Test
    void testGetAllAuthors() {
        // Arrange
        com.example.database.generated.tables.pojos.Author author1 = 
            new com.example.database.generated.tables.pojos.Author(1, "John", "Doe");

        com.example.database.generated.tables.pojos.Author author2 = 
            new com.example.database.generated.tables.pojos.Author(2, "Jane", "Smith");

        List<com.example.database.generated.tables.pojos.Author> mockAuthors = Arrays.asList(author1, author2);
        when(authorRepository.findAll()).thenReturn(mockAuthors);

        // Act
        List<com.example.database.generated.tables.pojos.Author> result = authorService.getAllAuthors();

        // Assert
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void testGetAuthorById_Found() {
        // Arrange
        com.example.database.generated.tables.pojos.Author author = 
            new com.example.database.generated.tables.pojos.Author(1, "John", "Doe");

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        // Act
        Optional<com.example.database.generated.tables.pojos.Author> result = authorService.getAuthorById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(authorRepository, times(1)).findById(1);
    }

    @Test
    void testGetAuthorById_NotFound() {
        // Arrange
        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<com.example.database.generated.tables.pojos.Author> result = authorService.getAuthorById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findById(999);
    }

    @Test
    void testCreateAuthor() {
        // Arrange
        AuthorCreateRequest newAuthor = new AuthorCreateRequest("John", "Doe");

        doNothing().when(authorRepository).save(newAuthor);

        // Act
        authorService.createAuthor(newAuthor);

        // Assert
        verify(authorRepository, times(1)).save(newAuthor);
    }

    @Test
    void testUpdateAuthor_Success() {
        // Arrange
        com.example.database.generated.tables.pojos.Author existingAuthor = 
            new com.example.database.generated.tables.pojos.Author(1, "John", "Doe");

        AuthorCreateRequest updatedDetails = new AuthorCreateRequest("Jane", "Smith");

        when(authorRepository.findById(1)).thenReturn(Optional.of(existingAuthor));
        doNothing().when(authorRepository).update(existingAuthor, updatedDetails);

        // Act
        authorService.updateAuthor(1, updatedDetails);

        // Assert
        verify(authorRepository, times(1)).findById(1);
        verify(authorRepository, times(1)).update(existingAuthor, updatedDetails);
    }

    @Test
    void testUpdateAuthor_NotFound() {
        // Arrange
        AuthorCreateRequest updatedDetails = new AuthorCreateRequest("Jane", "Smith");

        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorService.updateAuthor(999, updatedDetails));
        verify(authorRepository, times(1)).findById(999);
        verify(authorRepository, never()).update(any(), any());
    }

    @Test
    void testDeleteAuthor_Success() {
        // Arrange
        com.example.database.generated.tables.pojos.Author existingAuthor = 
            new com.example.database.generated.tables.pojos.Author(1, "John", "Doe");

        when(authorRepository.findById(1)).thenReturn(Optional.of(existingAuthor));
        doNothing().when(authorRepository).deleteById(1);

        // Act
        boolean result = authorService.deleteAuthor(1);

        // Assert
        assertTrue(result);
        verify(authorRepository, times(1)).findById(1);
        verify(authorRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteAuthor_NotFound() {
        // Arrange
        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = authorService.deleteAuthor(999);

        // Assert
        assertFalse(result);
        verify(authorRepository, times(1)).findById(999);
        verify(authorRepository, never()).deleteById(any());
    }

    @Test
    void testSearchAuthors() {
        // Arrange
        com.example.database.generated.tables.pojos.Author author1 = 
            new com.example.database.generated.tables.pojos.Author(1, "John", "Smith");

        com.example.database.generated.tables.pojos.Author author2 = 
            new com.example.database.generated.tables.pojos.Author(2, "Jane", "Smithson");

        List<com.example.database.generated.tables.pojos.Author> mockAuthors = Arrays.asList(author1, author2);
        when(authorRepository.findByLastNameContaining("Smith")).thenReturn(mockAuthors);

        // Act
        List<com.example.database.generated.tables.pojos.Author> result = authorService.searchAuthors("Smith");

        // Assert
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findByLastNameContaining("Smith");
    }
}
