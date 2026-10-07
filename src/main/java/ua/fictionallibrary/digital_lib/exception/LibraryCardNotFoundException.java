package ua.fictionallibrary.digital_lib.exception;

import ua.fictionallibrary.digital_lib.starter.DomainException;

public class LibraryCardNotFoundException extends DomainException {
    public LibraryCardNotFoundException(String message) {
        super(message);
    }
}
