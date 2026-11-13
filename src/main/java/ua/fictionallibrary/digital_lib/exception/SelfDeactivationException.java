package ua.fictionallibrary.digital_lib.exception;

import ua.fictionallibrary.digital_lib.starter.exception.DomainException;

public class SelfDeactivationException extends DomainException {
    public SelfDeactivationException(String message) {
        super(message);
    }
}