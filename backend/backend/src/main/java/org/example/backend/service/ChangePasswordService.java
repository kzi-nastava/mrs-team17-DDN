package org.example.backend.service;

import org.example.backend.dto.request.ChangePasswordRequestDto;
import org.example.backend.dto.request.ResetPasswordRequestDto;
import org.example.backend.dto.response.UserAuthResponseDto;
import org.example.backend.event.PasswordResetEmailEvent;
import org.example.backend.repository.PasswordResetTokenRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class ChangePasswordService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher events;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public ChangePasswordService(UserRepository users, PasswordEncoder passwordEncoder,
        PasswordResetTokenRepository passwordResetTokenRepository,
                                 ApplicationEventPublisher events) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.events = events;
    }

    public void changePassword(Long userId, ChangePasswordRequestDto req) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (req == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        String current = trim(req.getCurrentPassword());
        String next = trim(req.getNewPassword());
        String confirm = trim(req.getConfirmNewPassword());

        if (isBlank(current) || isBlank(next) || isBlank(confirm)) {
            throw new IllegalArgumentException("All password fields are required");
        }

        if (next.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        if (!next.equals(confirm)) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        UserAuthResponseDto user = users.findAuthById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        if (!passwordEncoder.matches(current, user.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String newHash = passwordEncoder.encode(next);
        int updated = users.updatePasswordHash(userId, newHash);
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Password update failed");
        }
    }

    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) return;

        users.findAuthByEmail(email.trim())
                .ifPresent(user -> {

                    String token = UUID.randomUUID().toString().replace("-", "");
                    Instant expiresAt = Instant.now().plus(Duration.ofMinutes(30));

                    passwordResetTokenRepository.createToken(user.id(), token, expiresAt);

                    String link = frontendBaseUrl + "/new-password?token=" + token;

                    events.publishEvent(
                            new PasswordResetEmailEvent(user.email(), link)
                    );
                });
    }

    public void resetPassword(ResetPasswordRequestDto req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String token = trim(req.getToken());
        String next = trim(req.getNewPassword());
        String confirm = trim(req.getConfirmNewPassword());

        if (isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        if (isBlank(next) || isBlank(confirm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (next.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters long");
        }
        if (!next.equals(confirm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        var tokenRow = passwordResetTokenRepository.findValidToken(token, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        UserAuthResponseDto user = users.findAuthById(tokenRow.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        String newHash = passwordEncoder.encode(next);
        int updated = users.updatePasswordHash(tokenRow.userId(), newHash);
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Password update failed");
        }

        int used = passwordResetTokenRepository.markUsed(token, Instant.now());
        if (used <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Token already used");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
