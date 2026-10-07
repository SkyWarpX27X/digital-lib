package ua.fictionallibrary.digital_lib.exception;

import ua.fictionallibrary.digital_lib.starter.exception.DomainException;

public class IllegalResourceTypeException extends DomainException {
    public IllegalResourceTypeException(String message) {
        super(message);
    }
}
