package org.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugAuthController {

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(Map.of(
                "authenticated", a != null && a.isAuthenticated(),
                "principal", a != null ? a.getPrincipal() : null,
                "authorities", a != null ? a.getAuthorities() : null,
                "name", a != null ? a.getName() : null
        ));
    }
}
