package org.example.backend.exception;

public class ActiveRideConflictException extends RuntimeException {
    public ActiveRideConflictException(String message) {
        super(message);
    }
}
