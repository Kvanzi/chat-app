package com.kvanzi.chatapp.common.exception;

import com.kvanzi.chatapp.auth.exception.RefreshAccessTokenException;
import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.exception.UsernameTakenException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseWrapper.badRequestEntity("Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        return ResponseWrapper.badRequestEntity("Validation failed", errors);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handle(UserNotFoundException e) {
        return ResponseWrapper.notFoundEntity(e.getMessage());
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ResponseWrapper<Void>> handle(UsernameTakenException e) {
        return ResponseWrapper.badRequestEntity(e.getMessage());
    }

    @ExceptionHandler(RefreshAccessTokenException.class)
    public ResponseEntity<ResponseWrapper<String>> handle(RefreshAccessTokenException e) {
        return ResponseWrapper.unauthorizedEntity(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseWrapper<Void>> handle(BadCredentialsException e) {
        return ResponseWrapper.badRequestEntity("Bad credentials");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handle(Exception e) {
        log.debug("", e);
        return ResponseWrapper.internalServerErrorEntity("Internal server error. Please contact support");
    }
}
