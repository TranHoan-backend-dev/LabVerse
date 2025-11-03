package com.se1853_jv.exception;

import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<WrapperApiResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(
                WrapperApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WrapperApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                WrapperApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<WrapperApiResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(
                WrapperApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WrapperApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        WrapperApiResponse response = new WrapperApiResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WrapperApiResponse> handleGlobalException(Exception ex) {
        return new ResponseEntity<>(
                WrapperApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}




