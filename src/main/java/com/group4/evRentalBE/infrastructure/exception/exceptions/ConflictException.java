package com.group4.evRentalBE.infrastructure.exception.exceptions;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
