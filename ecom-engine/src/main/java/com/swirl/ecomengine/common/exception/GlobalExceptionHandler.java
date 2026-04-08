package com.swirl.ecomengine.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // 400 — Validation errors (@Valid)
    // ============================================================
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
                        HttpStatus.BAD_REQUEST.value(),
                        errors.toString(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 400 — BadRequest
    // ============================================================
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Malformed request: " + ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 404 — NotFound
    // ============================================================
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 401 — Unauthorized
    // ============================================================
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 403 — Forbidden
    // ============================================================
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 409 — Conflict
    // ============================================================
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // Database-level constraint violations (unique keys, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Data integrity violation: " + ex.getMostSpecificCause().getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }

    // ============================================================
    // 500 — Unexpected errors
    // ============================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred",
                        LocalDateTime.now(),
                        request.getRequestURI()
                )
        );
    }
}