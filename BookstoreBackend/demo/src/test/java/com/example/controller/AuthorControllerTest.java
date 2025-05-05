package com.example.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.database.generatedclasses.tables.pojos.Author;
import com.example.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthorController.class)
class AuthorControllerTest {
    
    private static final Integer ID = 0;
    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";

    private static final String FIRST_NAME_PARAMETER = "firstName";
    private static final String LAST_NAME_PARAMETER = "lastName";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    public AuthorControllerTest(MockMvc mockMvc, ObjectMapper objectMapper){
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @MockitoBean
    private AuthorService service;

    @Test
    void testGetReturns200() throws Exception {
        // Setup
        List<Author> authors = new ArrayList<>();
        authors.add(new Author(ID, FIRST_NAME, LAST_NAME));
        final String expectedResponseContent = objectMapper.writeValueAsString(authors);

        // Mock
        when(service.get(FIRST_NAME, LAST_NAME)).thenReturn(authors);

        // Act / Assert
        mockMvc.perform(get("/author")
            .param(FIRST_NAME_PARAMETER, FIRST_NAME)
            .param(LAST_NAME_PARAMETER, LAST_NAME))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponseContent));
    }

    @Test
    void testGetReturns500() throws Exception {
        // Mock
        when(service.get("", "")).thenThrow(new RuntimeException());

        // Act / Assert
        mockMvc.perform(get("/author")
            .param(FIRST_NAME_PARAMETER, "")
            .param(LAST_NAME_PARAMETER, ""))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$").exists());
    }
}
