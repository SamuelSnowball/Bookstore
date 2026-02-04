package com.example.common.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test-endpoint");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void testHandleDataAccessException() {
        // Arrange
        DataAccessException exception = new DataAccessException("SQL constraint violation");

        // Act
        ProblemDetail problemDetail = exceptionHandler.handleInvalidInputException(exception, webRequest);

        // Assert
        assertNotNull(problemDetail);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("jOOQ exception", problemDetail.getDetail());
        // Should NOT contain SQL details for security
        assertFalse(problemDetail.getDetail().contains("SQL"));
        assertFalse(problemDetail.getDetail().contains("constraint"));
    }

    @Test
    void testDataAccessExceptionHidesSqlDetails() {
        // Arrange - Simulate jOOQ exception with SQL details
        DataAccessException exception = new DataAccessException(
            "Could not execute: INSERT INTO users (username, password) VALUES ('admin', 'secret123')"
        );

        // Act
        ProblemDetail problemDetail = exceptionHandler.handleInvalidInputException(exception, webRequest);

        // Assert
        assertEquals("jOOQ exception", problemDetail.getDetail());
        // Verify sensitive SQL is NOT exposed
        assertFalse(problemDetail.getDetail().contains("INSERT"));
        assertFalse(problemDetail.getDetail().contains("password"));
        assertFalse(problemDetail.getDetail().contains("secret123"));
    }
}
