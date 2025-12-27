package org.example.backend.controller;

import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/profile-change-requests")
public class AdminDriverProfileChangeController {

    @GetMapping
    public ResponseEntity<List<ProfileChangeRequestResponseDto>> listRequests(@RequestParam(required = false) String status, @RequestParam(required = false) String driverName) {

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ProfileChangeRequestResponseDto> getRequest(@PathVariable Long requestId) {
        ProfileChangeRequestResponseDto dto = new ProfileChangeRequestResponseDto();
        dto.setRequestId(requestId);
        dto.setDriverId(10L);
        dto.setStatus("PENDING");
        dto.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ProfileChangeRequestResponseDto> approve(@PathVariable Long requestId) {
        ProfileChangeRequestResponseDto dto = new ProfileChangeRequestResponseDto();
        dto.setRequestId(requestId);
        dto.setDriverId(10L);
        dto.setStatus("APPROVED");
        dto.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ProfileChangeRequestResponseDto> reject(@PathVariable Long requestId, @RequestParam(required = false) String reason) {

        ProfileChangeRequestResponseDto dto = new ProfileChangeRequestResponseDto();
        dto.setRequestId(requestId);
        dto.setDriverId(10L);
        dto.setStatus("REJECTED");
        dto.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        return ResponseEntity.ok(dto);
    }
}
