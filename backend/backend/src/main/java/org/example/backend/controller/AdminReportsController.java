package org.example.backend.controller;

import org.example.backend.dto.response.RideStatsReportResponseDto;
import org.example.backend.service.RideStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportsController {

    private final RideStatsService rideStatsService;

    public AdminReportsController(RideStatsService rideStatsService) {
        this.rideStatsService = rideStatsService;
    }

    @GetMapping("/rides")
    public ResponseEntity<RideStatsReportResponseDto> getAdminRideReport(
            @RequestParam String role,
            @RequestParam(required = false) Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(rideStatsService.buildAdminReport(role, userId, from, to));
    }
}
