package org.example.backend.controller;

import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.example.backend.repository.DriverProfileChangeRequestRepository;
import org.example.backend.service.DriverProfileChangeRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/profile-change-requests")
public class AdminDriverProfileChangeController {

    private final DriverProfileChangeRequestRepository repo;
    private final DriverProfileChangeRequestService service;

    public AdminDriverProfileChangeController(DriverProfileChangeRequestRepository repo,
                                              DriverProfileChangeRequestService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfileChangeRequestResponseDto>> listRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String driverName
    ) {
        List<DriverProfileChangeRequestRepository.DriverProfileChangeRequestRow> rows = repo.findAll(status);

        List<ProfileChangeRequestResponseDto> out = rows.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ProfileChangeRequestResponseDto> getRequest(@PathVariable Long requestId) {
        requireAdminUserId();

        return repo.findById(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ProfileChangeRequestResponseDto> approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String note
    ) {
        long adminId = requireAdminUserId();

        boolean ok = service.approve(requestId, adminId, note);
        if (!ok) return ResponseEntity.notFound().build();

        return repo.findById(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ProfileChangeRequestResponseDto> reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason
    ) {
        long adminId = requireAdminUserId();

        boolean ok = service.reject(requestId, adminId, reason);
        if (!ok) return ResponseEntity.notFound().build();

        return repo.findById(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ProfileChangeRequestResponseDto toDto(DriverProfileChangeRequestRepository.DriverProfileChangeRequestRow r) {
        ProfileChangeRequestResponseDto dto = new ProfileChangeRequestResponseDto();
        dto.setRequestId(r.id);
        dto.setDriverId(r.driverId);
        dto.setStatus(r.status);

        dto.setFirstName(r.firstName);
        dto.setLastName(r.lastName);
        dto.setAddress(r.address);
        dto.setPhoneNumber(r.phone);
        dto.setProfileImageUrl(r.profileImageUrl);

        OffsetDateTime odt = r.createdAt;
        LocalDateTime ldt = (odt != null) ? odt.toLocalDateTime() : LocalDateTime.now();
        dto.setCreatedAt(ldt);

        return dto;
    }

    private long requireAdminUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can access this endpoint");
        }

        try {
            return Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }
}
