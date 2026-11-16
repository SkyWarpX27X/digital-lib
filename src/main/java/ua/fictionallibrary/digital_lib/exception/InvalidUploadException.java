package ua.fictionallibrary.digital_lib.exception;

import ua.fictionallibrary.digital_lib.starter.exception.DomainException;

public class InvalidUploadException extends DomainException {
    public InvalidUploadException(String message) {
        super(message);
    }
}
