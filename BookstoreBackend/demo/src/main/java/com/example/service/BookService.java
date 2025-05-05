package com.example.service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.database.generatedclasses.tables.pojos.Book;
import com.example.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookRepository repository;

    public List<? extends Book> getBooks(boolean includeAuthors){
        return repository.getBooks(includeAuthors);
    }
    
    public Book insert(String authorId, String title){
        return repository.insert(Integer.parseInt(authorId), title);
    }
}