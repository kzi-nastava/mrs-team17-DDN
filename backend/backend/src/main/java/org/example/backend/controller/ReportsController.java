package org.example.backend.controller;

import org.example.backend.dto.response.RideStatsReportResponseDto;
import org.example.backend.service.RideStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final RideStatsService rideStatsService;

    public ReportsController(RideStatsService rideStatsService) {
        this.rideStatsService = rideStatsService;
    }

    @GetMapping("/rides")
    public ResponseEntity<RideStatsReportResponseDto> getMyRideReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isDriver = auth.getAuthorities().stream().anyMatch(a -> "ROLE_DRIVER".equals(a.getAuthority()));
        boolean isPassenger = auth.getAuthorities().stream().anyMatch(a -> "ROLE_PASSENGER".equals(a.getAuthority()));

        if (!isDriver && !isPassenger) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only DRIVER or PASSENGER can access this endpoint");
        }

        long userId;
        try {
            userId = Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        return ResponseEntity.ok(rideStatsService.buildMyReport(userId, isDriver, from, to));
    }
}
