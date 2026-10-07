package ua.fictionallibrary.digital_lib.starter;

public class DuplicateException extends DomainException {
    public DuplicateException(String message) {
        super(message);
    }
}
