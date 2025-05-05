package com.example.controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ValidatedController {
    /*
    Older javax validation:
	@GetMapping("/valid")
	public String valid(@Valid User user) {
		return "User was valid!";
	}
    */
    // Plain validated annotation
	@GetMapping("/validated")
	public String validated(@Validated(User.class) @RequestBody User user) {
		return "User was valid!";
	}
    // Annotations combined with json view
    class User {
        private String firstName;
        private String lastName;
        public User(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
    }
}