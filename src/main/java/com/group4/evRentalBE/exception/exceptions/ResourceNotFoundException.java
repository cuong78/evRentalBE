package com.group4.evRentalBE.exception.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    // Constructor với 1 tham số
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor với 2 tham số
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor với 3 tham số (giống như trong code ban đầu)
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
