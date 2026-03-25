package com.swirl.ecomengine.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------
    // Validation errors (400)
    // -----------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        errors.toString(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // -----------------------------
    // Custom NotFound exceptions (404)
    // -----------------------------
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // Catches JPA's own "not found"
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // -----------------------------
    // Data integrity (409)
    // -----------------------------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        "Data integrity violation: " + ex.getMostSpecificCause().getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }
}