package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AdminSetUserBlockRequestDto;
import org.example.backend.dto.response.AdminUserStatusResponseDto;
import org.example.backend.dto.response.AdminUserOptionResponseDto;
import org.example.backend.repository.AdminUserSelectRepository;
import org.example.backend.service.AdminUserManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

    private final AdminUserSelectRepository repo;
    private final AdminUserManagementService managementService;

    public AdminUsersController(AdminUserSelectRepository repo, AdminUserManagementService managementService) {
        this.repo = repo;
        this.managementService = managementService;
    }

    @GetMapping
    public List<AdminUserOptionResponseDto> listUsers(
            @RequestParam String role,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "200") int limit
    ) {
        String r = (role == null) ? "" : role.trim().toUpperCase();
        if (!r.equals("DRIVER") && !r.equals("PASSENGER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be DRIVER or PASSENGER");
        }

        int clamped = clamp(limit, 1, 500);
        return repo.listUsersByRole(r, query, clamped);
    }

    @GetMapping("/status")
    public List<AdminUserStatusResponseDto> listUsersWithStatus(
            @RequestParam String role,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return managementService.listUsersWithStatus(role, query, limit);
    }

    @PutMapping("/{userId}/block")
    public ResponseEntity<AdminUserStatusResponseDto> setBlockStatus(
            @PathVariable long userId,
            @Valid @RequestBody AdminSetUserBlockRequestDto request
    ) {
        return ResponseEntity.ok(managementService.setBlockStatus(userId, request));
    }

    private int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
