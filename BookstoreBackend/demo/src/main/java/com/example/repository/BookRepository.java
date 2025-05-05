package com.example.repository;
import static com.example.database.generatedclasses.Tables.AUTHOR;
import static com.example.database.generatedclasses.Tables.BOOK;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.database.generatedclasses.tables.daos.BookDao;
import com.example.database.generatedclasses.tables.pojos.Book;
import com.example.database.generatedclasses.tables.records.BookRecord;
import com.example.models.BookWithAuthor;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookRepository extends BookDao { 

    private final DSLContext create;

    public List<? extends Book> getBooks(boolean includeAuthors){
        if(includeAuthors){
            return create.select().from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .fetch().into(BookWithAuthor.class);
        }

        return create.select().from(BOOK).fetch().into(Book.class);
    }

    public Book insert(int authorId, String title) {
        BookRecord book = create.newRecord(BOOK);
        book.setAuthorId(authorId);
        book.setTitle(title);
        book.store();
        Book result = book.into(Book.class);
        return result;
    }
}