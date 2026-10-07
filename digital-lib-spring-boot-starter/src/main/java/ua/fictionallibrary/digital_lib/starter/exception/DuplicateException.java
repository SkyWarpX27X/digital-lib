package ua.fictionallibrary.digital_lib.starter.exception;

public class DuplicateException extends DomainException {
    public DuplicateException(String message) {
        super(message);
    }
}
