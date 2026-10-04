package ua.fictionallibrary.digital_lib.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataNotFoundException.class)
    public ProblemDetail handleDataNotFound(DataNotFoundException e) {
        log.error("Data not found", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("Data not found");
        problemDetail.setType(URI.create("urn:problem-type:data-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationFail(MethodArgumentNotValidException e) {
        log.error("Failed to validate data", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Failed to validate data");
        problemDetail.setType(URI.create("urn:problem-type:validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("Failed to parse request body", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body or unrecognized field");
        problemDetail.setTitle("Failed to parse request body");
        problemDetail.setType(URI.create("urn:problem-type:malformed-request"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(LibraryCardNotFoundException.class)
    public ProblemDetail handleLibraryCardNotFound(LibraryCardNotFoundException e) {
        log.error("Expected library card", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Library card not exists");
        problemDetail.setType(URI.create("urn:problem-type:data-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateException.class)
    public ProblemDetail handleDuplicate(DuplicateException e) {
        log.error("Duplicate data", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setTitle("Duplicate data");
        problemDetail.setType(URI.create("urn:problem-type:duplicate-data"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidOrderUpdateException.class)
    public ProblemDetail handleInvalidOrder(InvalidOrderUpdateException e) {
        log.error("Invalid order status", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Invalid order status");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(IllegalResourceTypeException.class)
    public ProblemDetail handleIllegalResourceType(IllegalResourceTypeException e) {
        log.error("Failed to validate resource type", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Failed to validate resource type");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
