package com.group4.evRentalBE.infrastructure.exception;

public class KYCException extends RuntimeException {
    public KYCException(String message) {
        super(message);
    }
    
    public KYCException(String message, Throwable cause) {
        super(message, cause);
    }
}
