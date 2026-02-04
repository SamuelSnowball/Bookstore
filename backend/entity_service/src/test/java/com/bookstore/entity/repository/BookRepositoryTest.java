package com.bookstore.entity.repository;

import static com.example.database.generated.Tables.BOOK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.common.repository.BaseIntegrationTest;
import com.example.database.generated.tables.pojos.Book;
import com.example.database.generated.tables.pojos.BookAuthorVw;
import com.example.database.generated.tables.records.BookRecord;

// Inherits @Transactional from BaseIntegrationTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MyDataSource.class, BookRepository.class })
class BookRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository repository;

    @Autowired
    private DSLContext create;

    private final int authorId = 1;
    private final String bookTitle = "Book";

    @Test
    void insertBookTest() {
        // Count books from table directly (not view)
        int initialCount = create.selectFrom(BOOK).fetch().into(Book.class).size();
        assertEquals(100000, initialCount);

        // Insert
        BookRecord book = create.newRecord(BOOK);
        book.setAuthorId(authorId);
        book.setTitle(bookTitle);
        book.store();

        // Expect
        int newCount = create.selectFrom(BOOK).fetch().into(Book.class).size();
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    void getBooks() {
        // Assert data loaded from liquibase - count from table directly
        List<Book> allBooks = create.selectFrom(BOOK).fetch().into(Book.class);
        assertEquals(100000, allBooks.size());

        // Test finding specific books by ID
        BookAuthorVw expectedBook1 = new BookAuthorVw(1, 2708, "The Forgotten Chronicles",
                new java.math.BigDecimal("16.31"),
                "A masterfully crafted story that weaves together loyalty and suspense.", "Charles", "Allan");

        repository.findById(1).ifPresent(book -> {
            assertEquals(expectedBook1.getId(), book.getId());
            assertEquals(expectedBook1.getTitle(), book.getTitle());
        });

        BookAuthorVw expectedBook11 = new BookAuthorVw(11, 1505, "The Secret Garden", new java.math.BigDecimal("20.99"),
                null, "Emily", "Hernandez");

        repository.findById(11).ifPresent(book -> {
            assertEquals(expectedBook11.getId(), book.getId());
            assertEquals(expectedBook11.getTitle(), book.getTitle());
        });
    }

    @Test
    void getBooksPaginated() {
        // Act - get first page (prevPageLastBookId = null or 0)
        List<BookAuthorVw> books = repository.findAll(null);

        // Assert - should return 10 books
        assertEquals(10, books.size());

        // Verify first book has ID 1
        assertEquals(1, books.get(0).getId());
        assertEquals("The Forgotten Chronicles", books.get(0).getTitle());

        // Verify last book in page has ID 10
        assertEquals(10, books.get(9).getId());

        // Act - get second page (books after ID 10)
        books = repository.findAll(10);

        // Assert - should return 10 books
        assertEquals(10, books.size());

        // Verify first book has ID 11
        assertEquals(11, books.get(0).getId());
        assertEquals("The Secret Garden", books.get(0).getTitle());

        // Verify last book in page has ID 20
        assertEquals(20, books.get(9).getId());
    }
}
