package org.example.backend.exception;

public class BlockedUserException extends RuntimeException {

    private final String blockReason;

    public BlockedUserException(String message, String blockReason) {
        super(message);
        this.blockReason = blockReason;
    }

    public String getBlockReason() {
        return blockReason;
    }
}
