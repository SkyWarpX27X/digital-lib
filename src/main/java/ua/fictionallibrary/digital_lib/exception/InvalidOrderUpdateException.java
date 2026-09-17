package ua.fictionallibrary.digital_lib.exception;

public class InvalidOrderUpdateException extends DomainException {
    public InvalidOrderUpdateException(String message) {
        super(message);
    }
}
