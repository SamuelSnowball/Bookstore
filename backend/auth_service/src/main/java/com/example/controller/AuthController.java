package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.security.JwtUtil;
import com.example.security.CustomUserDetailsService;
import com.example.common.repository.UserRepository;
import com.example.database.generated.tables.pojos.User;

import lombok.Data;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(final AuthenticationManager authManager,
            final CustomUserDetailsService userService,
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody final LoginRequest loginRequest) {
        // Check if user exists, if not create them
        User user = userService.getUserByUsername(loginRequest.getUsername());
        if (user == null) {
            // Create new user with constructor (id, username, password)
            user = new User(null, loginRequest.getUsername(), passwordEncoder.encode(loginRequest.getPassword()));
            userRepository.insert(user);
            // Retrieve the user again to get the generated ID
            user = userService.getUserByUsername(loginRequest.getUsername());
        }

        ResponseEntity<?> response;
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            final String token = jwtUtil.generateToken(loginRequest.getUsername(), user.getId());
            response = ResponseEntity.ok(new LoginResponse(token, loginRequest.getUsername(), user.getId()));

        } catch (final BadCredentialsException e) {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password"));
        }
        
        return response;
    }

    @Data
    /* default */ static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    /* default */ static class LoginResponse {
        private String token;
        private String username;
        private Integer userId;

        public LoginResponse(final String token, final String username, final Integer userId) {
            this.token = token;
            this.username = username;
            this.userId = userId;
        }
    }

    @Data
    /* default */ static class ErrorResponse {
        private String message;

        public ErrorResponse(final String message) {
            this.message = message;
        }
    }
}
