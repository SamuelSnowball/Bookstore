package com.example.common.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;

import org.jooq.Configuration;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class ExceptionTranslatorTest {

    private ExceptionTranslator exceptionTranslator;

    @Mock
    private ExecuteContext executeContext;

    @Mock
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        exceptionTranslator = new ExceptionTranslator();
    }

    @Test
    void testException_TranslatesSQLException() {
        // Arrange
        SQLException sqlException = new SQLException("Duplicate entry", "23000", 1062);
        when(executeContext.configuration()).thenReturn(configuration);
        when(configuration.dialect()).thenReturn(SQLDialect.MYSQL);
        when(executeContext.sqlException()).thenReturn(sqlException);
        when(executeContext.sql()).thenReturn("INSERT INTO users (username) VALUES ('duplicate')");

        // Act
        exceptionTranslator.exception(executeContext);

        // Assert
        ArgumentCaptor<DataAccessException> captor = ArgumentCaptor.forClass(DataAccessException.class);
        verify(executeContext).exception(captor.capture());
        
        DataAccessException translatedEx = captor.getValue();
        assertNotNull(translatedEx);
        // Spring's translator should convert MySQL duplicate entry to DuplicateKeyException
        assertTrue(translatedEx instanceof org.springframework.dao.DuplicateKeyException);
    }

    @Test
    void testException_IncludesTaskDescriptionInMessage() {
        // Arrange
        SQLException sqlException = new SQLException("Duplicate entry", "23000", 1062);
        when(executeContext.configuration()).thenReturn(configuration);
        when(configuration.dialect()).thenReturn(SQLDialect.MYSQL);
        when(executeContext.sqlException()).thenReturn(sqlException);
        when(executeContext.sql()).thenReturn("INSERT INTO test VALUES (1)");

        // Act
        exceptionTranslator.exception(executeContext);

        // Assert
        ArgumentCaptor<DataAccessException> captor = ArgumentCaptor.forClass(DataAccessException.class);
        verify(executeContext).exception(captor.capture());
        
        DataAccessException translatedEx = captor.getValue();
        assertNotNull(translatedEx);
        // Verify the task description "Access database using Jooq" appears in the exception message
        assertTrue(translatedEx.getMessage().contains("Access database using Jooq"),
                "Exception message should contain the task description");
    }

    @Test
    void testException_PreservesOriginalSQLException() {
        // Arrange
        SQLException originalException = new SQLException("Original message", "42000", 9999);
        when(executeContext.configuration()).thenReturn(configuration);
        when(configuration.dialect()).thenReturn(SQLDialect.MYSQL);
        when(executeContext.sqlException()).thenReturn(originalException);
        when(executeContext.sql()).thenReturn("INVALID SQL");

        // Act
        exceptionTranslator.exception(executeContext);

        // Assert
        ArgumentCaptor<DataAccessException> captor = ArgumentCaptor.forClass(DataAccessException.class);
        verify(executeContext).exception(captor.capture());
        
        DataAccessException translatedEx = captor.getValue();
        assertNotNull(translatedEx.getCause());
        assertEquals(originalException, translatedEx.getCause());
    }
}
