package org.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.backend.exception.NoAvailableDriverException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(NoAvailableDriverException.class)
    public ResponseEntity<Map<String, Object>> handleNoDriver(NoAvailableDriverException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", ex.getMessage()
        ));
    }
}
