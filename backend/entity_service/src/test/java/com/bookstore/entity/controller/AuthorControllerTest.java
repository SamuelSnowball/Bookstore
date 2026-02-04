package com.bookstore.entity.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bookstore.entity.models.AuthorCreateRequest;
import com.bookstore.entity.service.AuthorService;
import com.example.common.database.MyDataSource;
import com.example.common.repository.UserRepository;
import com.example.common.security.CustomJwtDecoder;
import com.example.common.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.integration.spring.SpringLiquibase;

@WebMvcTest(
    controllers = AuthorController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MyDataSource.class})
)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class AuthorControllerTest {
    
    private static final Integer ID = 1;
    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService service;
    
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
    void testGetAllAuthorsReturns200() throws Exception {
        // Setup
        List<com.example.database.generated.tables.pojos.Author> authors = new ArrayList<>();
        com.example.database.generated.tables.pojos.Author author = 
            new com.example.database.generated.tables.pojos.Author(ID, FIRST_NAME, LAST_NAME);
        authors.add(author);
        final String expectedResponseContent = objectMapper.writeValueAsString(authors);

        // Mock
        when(service.getAllAuthors()).thenReturn(authors);

        // Act / Assert
        mockMvc.perform(get("/author")
                .with(jwt().jwt(jwt -> jwt.claim("userId", ID))))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponseContent));
    }

    @Test
    void testGetAllAuthorsReturnsEmptyList() throws Exception {
        // Mock
        when(service.getAllAuthors()).thenReturn(new ArrayList<>());

        // Act / Assert
        mockMvc.perform(get("/author")
                .with(jwt().jwt(jwt -> jwt.claim("userId", ID))))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void testGetAllAuthorsReturns500() throws Exception {
        // Mock
        when(service.getAllAuthors()).thenThrow(new RuntimeException());
        
        // Act / Assert
        mockMvc.perform(get("/author")
                .with(jwt().jwt(jwt -> jwt.claim("userId", ID))))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreateAuthor_Returns201() throws Exception {
        AuthorCreateRequest req = new AuthorCreateRequest(FIRST_NAME, LAST_NAME);
        
        doNothing().when(service).createAuthor(req);
        
        mockMvc.perform(post("/author")
                .with(jwt().jwt(jwt -> jwt.claim("userId", ID)))
                .with(csrf())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }
}
