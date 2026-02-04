package com.example.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

// API responses that might be returned by all the methods
@ApiResponses(value = {
                @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                                @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)) }),
                @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
                                @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)) }),
                @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                                @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)) })
})
@RequiredArgsConstructor
public abstract class BaseController {

    /**
     * Extracts userId from the SecurityContext (JWT token automatically validated by Spring Security)
     * @return The userId from the JWT claims
     * @throws ResponseStatusException if authentication is not present
     */
    protected Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No valid authentication found");
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

}
