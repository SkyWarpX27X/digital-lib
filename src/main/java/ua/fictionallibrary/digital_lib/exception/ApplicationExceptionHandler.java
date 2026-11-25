package ua.fictionallibrary.digital_lib.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;

@Order(1)
@RestControllerAdvice
public class ApplicationExceptionHandler {
    private static final String TIMESTAMP_PROPERTY_NAME = "timestamp";
    private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(LibraryCardNotFoundException.class)
    public ProblemDetail handleLibraryCardNotFound(LibraryCardNotFoundException e) {
        log.error("Expected library card", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Library card not exists");
        problemDetail.setType(URI.create("urn:problem-type:data-not-found"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidOrderUpdateException.class)
    public ProblemDetail handleInvalidOrder(InvalidOrderUpdateException e) {
        log.error("Invalid order status", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Invalid order status");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(SelfDeactivationException.class)
    public ProblemDetail handleSelfDeactivation(SelfDeactivationException e) {
        log.error("Self deactivation attempt", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Self deactivation is not allowed");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(IllegalResourceTypeException.class)
    public ProblemDetail handleIllegalResourceType(IllegalResourceTypeException e) {
        log.error("Failed to validate resource type", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Failed to validate resource type");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.error("Upload too large", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "Uploaded file is too large");
        problemDetail.setTitle("Upload too large");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidUploadException.class)
    public ProblemDetail handleInvalidUpload(InvalidUploadException e) {
        log.error("Invalid upload", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Invalid upload");
        problemDetail.setType(URI.create("urn:problem-type:business-rule-error"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY_NAME, Instant.now());
        return problemDetail;
    }
}
