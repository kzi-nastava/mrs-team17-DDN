package org.example.backend.controller;

import org.example.backend.dto.request.ResetPasswordRequestDto;
import org.example.backend.service.ChangePasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final ChangePasswordService changePasswordService;

    public PasswordResetController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    // 1) Request reset email (public)
    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestParam("email") String email) {
        changePasswordService.requestPasswordReset(email);
        return ResponseEntity.accepted().build(); // 202 always
    }

    // 2) Confirm reset (public) - token + new password
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ResetPasswordRequestDto request) {
        changePasswordService.resetPassword(request);
        return ResponseEntity.noContent().build(); // 204
    }
}
