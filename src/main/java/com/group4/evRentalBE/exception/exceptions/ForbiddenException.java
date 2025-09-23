package com.group4.evRentalBE.exception.exceptions;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
