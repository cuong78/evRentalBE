package com.group4.evRentalBE.exception;


import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.exception.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.IllegalArgumentException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class APIHandle {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errorMessages.add(error.getField() + ": " + error.getDefaultMessage()));

        String combinedMessage = String.join(", ", errorMessages);

        return ResponseEntity.badRequest()
                .body(new ResponseObject(
                        HttpStatus.BAD_REQUEST.value(),
                        combinedMessage,
                        null
                        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseObject> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errorMessages = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errorMessages.add(field + ": " + violation.getMessage());
        });

        String combinedMessage = String.join(", ", errorMessages);

        return ResponseEntity.badRequest()
                .body(new ResponseObject(HttpStatus.BAD_REQUEST.value(), combinedMessage, null));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ResponseObject> handleDuplicate(SQLIntegrityConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResponseObject> handleNullPointer(NullPointerException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseObject> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseObject> handleRuntimeExceptionException(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseObject> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseObject> handleConflictException(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseObject> handleBadRequestException(BadRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseObject> handleForbiddenException(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ResponseObject> handleTokenRefreshException(TokenRefreshException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ResponseObject> handleTokenRefreshException(InternalServerErrorException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseObject> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseObject> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }
}
