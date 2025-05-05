package com.example;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.database.generatedclasses.tables.pojos.Author;
import com.example.repository.AuthorRepository;
import com.example.service.AuthorService;
/*
 * I originally used data from the
 * author objects, but it's confusing as changing the data within the author objects doesn't impact the tests.
 * So instead I'm using variables like: String authorFirstName = "John"
 * 
 * Is there any valuing asserting this? assertEquals(2, authorList.size()); 
 * It's minor but tests we are returning values from the get method properly.
 */
@ExtendWith(MockitoExtension.class) // JUnit 5, no explicit usage of MockitoAnnotations.openMocks(this);
public class ServiceLayerTest {
    @Mock
    private AuthorRepository authorRepository;
    @InjectMocks
    private AuthorService service;
    @Test
    void testGetAuthorsByName() {
        // Arrange
        List<Author> authors = new ArrayList<>();
        Author author1 = new Author();
        Author author2 = new Author();
        authors.add(author1);
        authors.add(author2);
        String authorFirstName = "John";
        String authorLastName = "Wick";
        when(authorRepository.getByName(authorFirstName, authorLastName)).thenReturn(authors);
        // Test
        List<Author> authorList = service.get(authorFirstName, authorLastName);
        // Assert
        assertEquals(2, authorList.size());
        verify(authorRepository, times(1))
                .getByName(authorFirstName, authorLastName);
    }
    @Test
    void testGetAuthorsByFirstName() {
        // Arrange
        List<Author> authors = new ArrayList<>();
        Author author1 = new Author();
        Author author2 = new Author();
        authors.add(author1);
        authors.add(author2);
        String authorFirstName = "John";
        when(authorRepository.fetchByFirstName(authorFirstName)).thenReturn(authors);
        // Test
        List<Author> authorList = service.get(authorFirstName, null);
        // Assert
        assertEquals(2, authorList.size());
        verify(authorRepository, times(1))
                .fetchByFirstName(authorFirstName);
    }
    @Test
    void testGetAuthorsByLastName() {
        // Arrange
        List<Author> authors = new ArrayList<>();
        Author author1 = new Author();
        Author author2 = new Author();
        authors.add(author1);
        authors.add(author2);
        String authorLastName = "Wick";
        when(authorRepository.fetchByLastName(authorLastName)).thenReturn(authors);
        // Test
        List<Author> authorList = service.get(null, authorLastName);
        // Assert
        assertEquals(2, authorList.size());
        verify(authorRepository, times(1))
                .fetchByLastName(authorLastName);
    }
    @Test
    void testGetAuthors() {
        // Arrange
        List<Author> authors = new ArrayList<>();
        Author author1 = new Author();
        Author author2 = new Author();
        authors.add(author1);
        authors.add(author2);
        when(authorRepository.getAuthors()).thenReturn(authors);
        // Test
        List<Author> authorList = service.get(null, null);
        // Assert
        assertEquals(2, authorList.size());
        verify(authorRepository, times(1))
                .getAuthors();
    }
}