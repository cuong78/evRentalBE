package com.group4.evRentalBE.infrastructure.exception.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
