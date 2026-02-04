package com.example.common.exception;

import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleInvalidInputException(RuntimeException e, WebRequest request) {

        ProblemDetail problemDetail = null;

        // Error originated from JOOQ, don't put SQL in the response to the client
        if (e instanceof DataAccessException) {
            log.error("Caught jOOQ exception");
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "jOOQ exception");
        } else {
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        // Other properties...
        // problemDetail.setInstance(URI.create("discount"));
        return problemDetail;
    }

}
