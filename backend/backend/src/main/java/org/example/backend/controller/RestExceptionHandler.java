package org.example.backend.controller;

import org.example.backend.exception.ActiveRideConflictException;
import org.example.backend.exception.NoAvailableDriverException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    public record ErrorResponse(String message) {}

    @ExceptionHandler(NoAvailableDriverException.class)
    public ResponseEntity<ErrorResponse> handleNoAvailableDriver(NoAvailableDriverException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ActiveRideConflictException.class)
    public ResponseEntity<ErrorResponse> handleActiveRideConflict(ActiveRideConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
