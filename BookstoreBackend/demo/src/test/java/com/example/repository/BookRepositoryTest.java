package com.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static com.example.database.generatedclasses.Tables.BOOK;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.example.database.MyDataSource;
import com.example.database.generatedclasses.tables.pojos.Book;
import com.example.database.generatedclasses.tables.records.BookRecord;

@ContextConfiguration(classes = {MyDataSource.class, BookRepository.class})
@PropertySource({"classpath:test_db.properties"})
@ExtendWith(SpringExtension.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    @Autowired
    private DSLContext create;

    private final int AUTHOR_ID = 1;
    private final String BOOK_TITLE = "Book";

    @Test
    @Transactional
    public void insertFirstNameTest(){
        // Ensure clean DB state
        assertEquals(0, repository.findAll().size());
        
        // Insert
        BookRecord book = create.newRecord(BOOK);
        book.setAuthorId(AUTHOR_ID);
        book.setTitle(BOOK_TITLE);
        book.store();
        Book result = book.into(Book.class);
        repository.insert(result);
            
        // Expect
        assertEquals(1, repository.findAll().size());
    }
}
