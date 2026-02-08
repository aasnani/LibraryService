package com.library.api;

import com.library.api.response.ErrorResponse;
import com.library.exceptions.LibraryException;
import com.library.exceptions.LibraryException.LibraryExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for Library API.
 *
 * <p>Converts exceptions thrown from controllers and services into
 * structured {@link ErrorResponse} objects for consistent API error responses.</p>
 */
@RestControllerAdvice
public class LibraryExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(LibraryExceptionHandler.class);

    @ExceptionHandler(LibraryException.class)
    public ResponseEntity<ErrorResponse> handleLibraryException(LibraryException ex, HttpServletRequest request) {
        HttpStatus status;

        switch (ex.getType()) {
            case BOOK_NOT_FOUND, MEMBER_NOT_FOUND -> status = HttpStatus.NOT_FOUND;
            case BOOK_CANNOT_REDUCE_COPIES,
                 LIBRARY_BOOK_CANNOT_DELETE_ACTIVE_LOANS,
                 LIBRARY_MEMBER_CANNOT_DELETE_ACTIVE_LOANS,
                 LIBRARY_MEMBER_MAX_ACTIVE_LOANS_EXCEEDED,
                 LIBRARY_MEMBER_ALREADY_HAS_BOOK,
                 BOOK_NO_AVAILABLE_COPIES,
                 LIBRARY_MEMBER_BLOCKED_DUE_TO_OVERDUE,
                 LIBRARY_NO_ACTIVE_LOAN -> status = HttpStatus.BAD_REQUEST;
            default -> status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse body = new ErrorResponse(
                status.value(),
                ex.getType().name(),
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("LibraryException: type={}, message={}, path={}", ex.getType(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "DATA_INTEGRITY_VIOLATION",
                "Operation could not be completed due to data constraints.",
                request.getRequestURI()
        );

        log.error("DataIntegrityViolation: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResultException(EmptyResultDataAccessException ex,
                                                                    HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                "The requested resource does not exist.",
                request.getRequestURI()
        );

        log.warn("Resource not found: path={}, message={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {

        String errors = ex.getBindingResult()
                          .getFieldErrors()
                          .stream()
                          .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                          .collect(Collectors.joining(", "));

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                errors,
                request.getRequestURI()
        );

        log.info("Validation error: path={}, errors={}", request.getRequestURI(), errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please contact support.",
                request.getRequestURI()
        );

        log.error("Unhandled exception: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
