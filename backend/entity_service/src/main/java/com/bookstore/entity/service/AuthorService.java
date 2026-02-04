package com.bookstore.entity.service;

import com.bookstore.entity.models.AuthorCreateRequest;

import lombok.RequiredArgsConstructor;

import com.bookstore.entity.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<com.example.database.generated.tables.pojos.Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Optional<com.example.database.generated.tables.pojos.Author> getAuthorById(Integer id) {
        return authorRepository.findById(id);
    }

    public void createAuthor(AuthorCreateRequest author) {
        authorRepository.save(author);
    }

    public void updateAuthor(Integer id, AuthorCreateRequest authorDetails) {
        com.example.database.generated.tables.pojos.Author existing = authorRepository.findById(id).orElse(null);

        if (existing != null) {
            authorRepository.update(existing, authorDetails);
        } else {
            throw new RuntimeException("Author not found with id: " + id);
        }
    }

    public boolean deleteAuthor(Integer id) {
        return authorRepository.findById(id)
                .map(author -> {
                    authorRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    public List<com.example.database.generated.tables.pojos.Author> searchAuthors(String lastName) {
        return authorRepository.findByLastNameContaining(lastName);
    }
}
