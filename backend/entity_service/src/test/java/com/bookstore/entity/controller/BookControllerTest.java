package com.bookstore.entity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.bookstore.entity.models.BookCreateRequest;
import com.bookstore.entity.service.BookService;
import com.example.common.database.MyDataSource;
import com.example.common.repository.UserRepository;
import com.example.common.security.CustomJwtDecoder;
import com.example.common.security.JwtUtil;
import com.example.database.generated.tables.pojos.BookAuthorVw;
import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.integration.spring.SpringLiquibase;

@WebMvcTest(
    controllers = BookController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MyDataSource.class})
)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class BookControllerTest {

    private static final int BOOK_ID = 1;
    private static final int AUTHOR_ID = 1;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    private DSLContext dsl;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private SpringLiquibase liquibase;

    @Test
    void testGetAllBooks_Returns200() throws Exception {
        // Setup
        List<BookAuthorVw> books = new ArrayList<>();
        BookAuthorVw book = new BookAuthorVw(
            BOOK_ID, AUTHOR_ID, "Test Book", BigDecimal.TEN, "A test book", "John", "Doe"
        );
        books.add(book);
        final String expectedResponseContent = objectMapper.writeValueAsString(books);
        // Mock
        when(service.getAllBooks(0)).thenReturn(books);
        // Act / Assert
        mockMvc.perform(get("/book")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseContent));
        verify(service, times(1)).getAllBooks(0);
    }

    @Test
    void testGetAllBooks_WithPagination_Returns200() throws Exception {
        // Setup
        List<BookAuthorVw> books = new ArrayList<>();
        BookAuthorVw book1 = new BookAuthorVw(
            11, AUTHOR_ID, "Test Book 11", BigDecimal.TEN, "A test book", "John", "Doe"
        );
        BookAuthorVw book2 = new BookAuthorVw(
            12, AUTHOR_ID, "Test Book 12", BigDecimal.TEN, "Another test book", "Jane", "Smith"
        );
        books.add(book1);
        books.add(book2);
        final String expectedResponseContent = objectMapper.writeValueAsString(books);
        // Mock
        when(service.getAllBooks(10)).thenReturn(books);
        // Act / Assert
        mockMvc.perform(get("/book")
                .param("prevPageLastBookId", "10")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseContent));
        verify(service, times(1)).getAllBooks(10);
    }

    @Test
    void testGetAllBooks_ThrowsReturns500() throws Exception {
        // Mock
        when(service.getAllBooks(0)).thenThrow(new RuntimeException());

        // Act / Assert
        mockMvc.perform(get("/book"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(
                        "{type:\"about:blank\", title:\"Internal Server Error\", status:500, instance:\"/book\"}"));

        verify(service, times(1)).getAllBooks(0);
    }

    @Test
    void testCreateBook_Returns201() throws Exception {
        BookCreateRequest requestBook = new BookCreateRequest(
            AUTHOR_ID, "Test Book", BigDecimal.TEN, "A test book"
        );

        doNothing().when(service).createBook(any(BookCreateRequest.class));

        // Act / Assert
        mockMvc.perform(
            MockMvcRequestBuilders.post("/book")
                .with(jwt().jwt(jwt -> jwt.claim("userId", 1)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBook)))
            .andExpect(status().isCreated());

        verify(service, times(1)).createBook(any());
    }

    @Test
    void testCreateBook_Returns400() throws Exception {
        // Act / Assert
        mockMvc.perform(
                MockMvcRequestBuilders.post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(service, times(0)).createBook(null);
    }
}
