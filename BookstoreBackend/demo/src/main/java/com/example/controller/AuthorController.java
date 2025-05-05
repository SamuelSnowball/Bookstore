package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.database.generatedclasses.tables.pojos.Author;
import com.example.service.AuthorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorController {

	private final AuthorService authorService;

	// Get all authors, with some optional query parameters
	@GetMapping
	public List<Author> get(
		@RequestParam(required = false) String firstName,
		@RequestParam(required = false) String lastName) {
		return authorService.get(firstName, lastName);
	}

	@PostMapping
	public Author create(
		@RequestBody Map<String, String> names) {
		return authorService.insert(names.get("firstName"), names.get("lastName"));
	}
	
	@GetMapping("/getBooksForAuthor")
	public void getBooksForAuthor(
		@RequestParam String firstName,
		@RequestParam String lastName) {
		authorService.selectIntoMultipleObjects(firstName, lastName);
	}
}