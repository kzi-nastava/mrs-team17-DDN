package org.example.backend.controller;

import java.util.Map;

import org.example.backend.exception.ActiveRideConflictException;
import org.example.backend.exception.NoAvailableDriverException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(DuplicateKeyException.class)
public ResponseEntity<Map<String, Object>> handleDuplicateKey(DuplicateKeyException ex) {
return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
"message", ex.getMostSpecificCause().getMessage()
));
}


@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
"message", ex.getMostSpecificCause().getMessage()
));
}

    @ExceptionHandler(ActiveRideConflictException.class)
    public ResponseEntity<Map<String, Object>> handleActiveRideConflict(ActiveRideConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", ex.getMessage()
        ));
    }
}
