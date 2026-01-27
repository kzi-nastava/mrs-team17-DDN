package org.example.backend.controller;

import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.example.backend.repository.DriverProfileChangeRequestRepository;
import org.example.backend.service.DriverProfileChangeRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/profile-change-requests")
public class AdminDriverProfileChangeController {

    private final DriverProfileChangeRequestRepository repo;
    private final DriverProfileChangeRequestService service;

    private static final Long ADMIN_ID = 1L;

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
        return repo.findByIdForUpdate(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ProfileChangeRequestResponseDto> approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String note
    ) {
        boolean ok = service.approve(requestId, ADMIN_ID, note);

        if (!ok) {
            return ResponseEntity.notFound().build();
        }

        return repo.findByIdForUpdate(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ProfileChangeRequestResponseDto> reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason
    ) {
        boolean ok = service.reject(requestId, ADMIN_ID, reason);

        if (!ok) {
            return ResponseEntity.notFound().build();
        }

        return repo.findByIdForUpdate(requestId)
                .map(r -> ResponseEntity.ok(toDto(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ProfileChangeRequestResponseDto toDto(DriverProfileChangeRequestRepository.DriverProfileChangeRequestRow r) {
        ProfileChangeRequestResponseDto dto = new ProfileChangeRequestResponseDto();
        dto.setRequestId(r.id);
        dto.setDriverId(r.driverId);
        dto.setStatus(r.status);

        OffsetDateTime odt = r.createdAt;
        LocalDateTime ldt = (odt != null) ? odt.toLocalDateTime() : LocalDateTime.now();
        dto.setCreatedAt(ldt);

        return dto;
    }
}
