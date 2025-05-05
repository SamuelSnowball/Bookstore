package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.database.generatedclasses.tables.pojos.Author;
import com.example.repository.AuthorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository repository;

    public List<Author> get(String firstName, String lastName){
        if(firstName != null && lastName != null){
            return repository.getByName(firstName, lastName);
        }
        else if(firstName != null){
            return repository.fetchByFirstName(firstName);
        }
        else if(lastName != null){
            return repository.fetchByLastName(lastName);
        }
        else {
            return repository.getAuthors();
        }
    }
    public List<String> getFirstNames() {
        return repository.getFirstNames();
    }
    public Author insert(String firstName, String lastName){
        return repository.insert(firstName, lastName);
    }
    public void selectIntoMultipleObjects(String firstName, String lastName){
        repository.selectIntoMultipleObjects(firstName, lastName);
    }
}