package com.bookstore.entity.repository;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.bookstore.entity.models.AuthorCreateRequest;
import com.example.database.generated.tables.Author;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor    
public class AuthorRepository {

    private final DSLContext dsl;

    public List<com.example.database.generated.tables.pojos.Author> findAll() {
        return dsl.selectFrom(Author.AUTHOR)
                .fetch().into(com.example.database.generated.tables.pojos.Author.class);
    }

    public Optional<com.example.database.generated.tables.pojos.Author> findById(Integer id) {
        return Optional.ofNullable(
                dsl.selectFrom(Author.AUTHOR)
                        .where(Author.AUTHOR.ID.eq(id))
                        .fetchOne().into(com.example.database.generated.tables.pojos.Author.class));
    }

    public void save(AuthorCreateRequest author) {
        dsl.insertInto(Author.AUTHOR)
                .set(dsl.newRecord(Author.AUTHOR, author))
                .execute();
    }

    public void update(com.example.database.generated.tables.pojos.Author existing, AuthorCreateRequest authorCreateRequest) {
        dsl.update(Author.AUTHOR)
                .set(Author.AUTHOR.FIRST_NAME, authorCreateRequest.getFirstName())
                .set(Author.AUTHOR.LAST_NAME, authorCreateRequest.getLastName())
                .where(Author.AUTHOR.ID.eq(existing.getId()))
                .execute();
    }

    public void deleteById(Integer id) {
        dsl.deleteFrom(Author.AUTHOR)
                .where(Author.AUTHOR.ID.eq(id))
                .execute();
    }

    public List<com.example.database.generated.tables.pojos.Author> findByLastNameContaining(String lastName) {
        return dsl.selectFrom(Author.AUTHOR)
                .where(Author.AUTHOR.LAST_NAME.containsIgnoreCase(lastName))
                .fetch().into(com.example.database.generated.tables.pojos.Author.class);
    }
}
