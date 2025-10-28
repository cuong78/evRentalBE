package com.group4.evRentalBE.infrastructure.exception.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
