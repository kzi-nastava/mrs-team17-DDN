package org.example.backend.service;

import org.example.backend.dto.request.ChangePasswordRequestDto;
import org.example.backend.dto.response.UserAuthResponseDto;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ChangePasswordService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public ChangePasswordService(UserRepository users, PasswordEncoder passwordEncoder, MailService mailService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
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
                    String token = UUID.randomUUID().toString();

                    String link = frontendBaseUrl + "/reset-password?token=" + token;

                    mailService.sendPasswordResetEmail(user.email(), link);
                });
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
