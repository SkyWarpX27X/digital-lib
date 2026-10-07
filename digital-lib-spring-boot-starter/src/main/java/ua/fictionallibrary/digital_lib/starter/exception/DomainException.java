package ua.fictionallibrary.digital_lib.starter.exception;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
