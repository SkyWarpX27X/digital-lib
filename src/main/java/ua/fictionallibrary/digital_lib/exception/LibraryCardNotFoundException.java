package ua.fictionallibrary.digital_lib.exception;

public class LibraryCardNotFoundException extends DomainException {
    public LibraryCardNotFoundException(String message) {
        super(message);
    }
}
