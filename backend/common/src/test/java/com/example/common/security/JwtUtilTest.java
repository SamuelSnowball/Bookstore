package com.example.common.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_USERNAME = "testuser";
    private static final Integer TEST_USER_ID = 123;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void testExtractUserId() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        Integer extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(TEST_USER_ID, extractedUserId);
    }

    @Test
    void testExtractExpiration() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);
        Date now = new Date();

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(now));
        // Token should expire in ~24 hours (86400000 ms)
        long timeDiff = expiration.getTime() - now.getTime();
        assertTrue(timeDiff > 86000000 && timeDiff < 87000000); // Allow some tolerance
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testTokenContainsCorrectClaims() {
        // Arrange & Act
        String token = jwtUtil.generateToken("john.doe", 456);

        // Assert
        assertEquals("john.doe", jwtUtil.extractUsername(token));
        assertEquals(456, jwtUtil.extractUserId(token));
    }

    @Test
    void testMultipleTokensAreUnique() throws InterruptedException {
        // Act - Add small delay to ensure different timestamps
        String token1 = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);
        Thread.sleep(1000); // Wait 1 second to ensure different iat timestamp (JWT uses seconds precision)
        String token2 = jwtUtil.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Assert - Different timestamps make them unique
        assertNotEquals(token1, token2);
    }

    @Test
    void testExtractUsername_DifferentUsers() {
        // Arrange
        String token1 = jwtUtil.generateToken("user1", 1);
        String token2 = jwtUtil.generateToken("user2", 2);

        // Act & Assert
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
        assertEquals(1, jwtUtil.extractUserId(token1));
        assertEquals(2, jwtUtil.extractUserId(token2));
    }
}
