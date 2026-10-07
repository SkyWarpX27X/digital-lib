package ua.fictionallibrary.digital_lib.exception;

import ua.fictionallibrary.digital_lib.starter.DomainException;

public class InvalidOrderUpdateException extends DomainException {
    public InvalidOrderUpdateException(String message) {
        super(message);
    }
}
