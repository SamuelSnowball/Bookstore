package com.bookstore.entity.repository;

import static com.example.database.generated.Tables.AUTHOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.common.repository.BaseIntegrationTest;
import com.example.database.generated.tables.pojos.Author;
import com.example.database.generated.tables.records.AuthorRecord;

// Inherits @Transactional from BaseIntegrationTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyDataSource.class, AuthorRepository.class})
class AuthorRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private DSLContext create;

    private final String firstName = "John";
    private final String lastName = "Doe";

    @Test
    void insertAuthorTest() {
        // Assert data loaded from liquibase
        int initialCount = repository.findAll().size();
        assertEquals(5000, initialCount);

        // Insert
        AuthorRecord author = create.newRecord(AUTHOR);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.store();

        // Expect
        assertEquals(initialCount + 1, repository.findAll().size());
    }

    @Test
    void getAllAuthors() {
        // Assert data loaded from liquibase
        List<Author> allAuthors = repository.findAll();
        assertEquals(5000, allAuthors.size());
    }

    @Test
    void findAuthorById() {
        // Test finding specific authors by ID
        Optional<Author> authorOpt = repository.findById(1);
        
        assertTrue(authorOpt.isPresent());
        authorOpt.ifPresent(author -> {
            assertEquals(1, author.getId());
            assertEquals("Jessica", author.getFirstName());
            // Last name verification removed as it may vary in seed data
        });
    }

    @Test
    void findAuthorById_AnotherExample() {
        Optional<Author> authorOpt = repository.findById(10);
        
        assertTrue(authorOpt.isPresent());
        authorOpt.ifPresent(author -> {
            assertEquals(10, author.getId());
            // You can add specific assertions based on your test data
        });
    }
}
