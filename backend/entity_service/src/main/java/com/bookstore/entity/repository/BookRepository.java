package com.bookstore.entity.repository;

import static com.example.database.generated.tables.Book.BOOK;
import static com.example.database.generated.tables.BookAuthorVw.BOOK_AUTHOR_VW;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.bookstore.entity.models.BookCreateRequest;
import com.example.database.generated.tables.pojos.BookAuthorVw;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final DSLContext dsl;

    public List<BookAuthorVw> findAll(Integer prevPageLastBookId) {
        var query = dsl.selectFrom(BOOK_AUTHOR_VW);
        
        if (prevPageLastBookId != null && prevPageLastBookId > 0) {
            return query.where(BOOK_AUTHOR_VW.ID.greaterThan(prevPageLastBookId))
                    .limit(10)
                    .fetch()
                    .into(BookAuthorVw.class);
        }
        
        return query.limit(10)
                .fetch()
                .into(BookAuthorVw.class);
    }

    public Optional<BookAuthorVw> findById(Integer id) {
        return Optional.ofNullable(
                dsl.selectFrom(BOOK_AUTHOR_VW)
                        .where(BOOK_AUTHOR_VW.ID.eq(id))
                        .fetchOne().into(BookAuthorVw.class));
    }

    // ? 
    public void update(BookAuthorVw existing, BookCreateRequest bookCreateRequest) {
        dsl.update(BOOK)
                .set(BOOK.TITLE, bookCreateRequest.getTitle())
                .set(BOOK.AUTHOR_ID, bookCreateRequest.getAuthorId())
                .where(BOOK.ID.eq(existing.getId()))
                .execute();
    }

    public void save(BookCreateRequest bookCreateRequest) {
        // We're creating
        dsl.insertInto(BOOK)
                .set(dsl.newRecord(BOOK, bookCreateRequest))
                .execute();
    }

    public void deleteById(Integer id) {
        dsl.deleteFrom(BOOK)
                .where(BOOK.ID.eq(id))
                .execute();
    }

    public List<BookAuthorVw> findByTitleContaining(String title) {
        return dsl.selectFrom(BOOK_AUTHOR_VW)
                .where(BOOK_AUTHOR_VW.TITLE.containsIgnoreCase(title))
                .fetch().into(BookAuthorVw.class);
    }
}
