package com.group4.evRentalBE.infrastructure.exception.exceptions;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
