package org.example.backend.controller;

import org.example.backend.dto.request.ChangePasswordRequestDto;
import org.example.backend.service.ChangePasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final ChangePasswordService changePasswordService;

    public AccountController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequestDto request
    ) {
        Long userId = extractUserId(authentication);
        changePasswordService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object p = authentication.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof Integer i) return i.longValue();
        if (p instanceof String s) {
            try { return Long.parseLong(s); } catch (Exception ignored) {}
        }
        return null;
    }
}
