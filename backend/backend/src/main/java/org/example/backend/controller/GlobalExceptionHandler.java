package org.example.backend.controller;

import java.util.Map;

import org.example.backend.exception.ActiveRideConflictException;
import org.example.backend.exception.BlockedUserException;
import org.example.backend.exception.NoAvailableDriverException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlockedUserException.class)
    public ResponseEntity<Map<String, Object>> handleBlockedUser(BlockedUserException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "message", ex.getMessage(),
                "blockReason", ex.getBlockReason()
        ));
    }

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
                "message", friendlyDbMessage(ex)
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", friendlyDbMessage(ex)
        ));
    }

    @ExceptionHandler(ActiveRideConflictException.class)
    public ResponseEntity<Map<String, Object>> handleActiveRideConflict(ActiveRideConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", ex.getMessage()
        ));
    }

    private String friendlyDbMessage(DataAccessException ex) {
        String direct = ex.getMessage();
        if (direct != null) {
            String low = direct.toLowerCase();
            if (!low.contains("duplicate key") && !low.contains("violates unique constraint") && direct.length() < 180) {
                return direct;
            }
        }

        String raw = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String m = raw == null ? "" : raw.toLowerCase();

        if (m.contains("ux_favorite_routes_user_route")) {
            return "The route already exists in your favorites.";
        }

        if (m.contains("favorite_routes") && m.contains("duplicate key")) {
            return "The route already exists in your favorites.";
        }

        return "The request cannot be processed (incorrect or conflicting data).";
    }
}
