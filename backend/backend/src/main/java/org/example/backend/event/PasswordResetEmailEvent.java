package org.example.backend.event;

public record PasswordResetEmailEvent(String email, String link) {}