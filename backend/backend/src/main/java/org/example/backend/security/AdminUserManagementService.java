package org.example.backend.service;

import org.example.backend.dto.request.AdminSetUserBlockRequestDto;
import org.example.backend.dto.response.AdminUserStatusResponseDto;
import org.example.backend.repository.AdminUserManagementRepository;
import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.DriverRideRepository;
import org.example.backend.repository.UserLookupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminUserManagementService {

    private final AdminUserManagementRepository repo;
    private final UserLookupRepository userLookupRepository;
    private final DriverRepository driverRepository;
    private final DriverRideRepository driverRideRepository;

    public AdminUserManagementService(AdminUserManagementRepository repo,
                                      UserLookupRepository userLookupRepository,
                                      DriverRepository driverRepository,
                                      DriverRideRepository driverRideRepository) {
        this.repo = repo;
        this.userLookupRepository = userLookupRepository;
        this.driverRepository = driverRepository;
        this.driverRideRepository = driverRideRepository;
    }

    public List<AdminUserStatusResponseDto> listUsersWithStatus(String role, String query, int limit) {
        String r = (role == null) ? "" : role.trim().toUpperCase();
        if (!"DRIVER".equals(r) && !"PASSENGER".equals(r)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be DRIVER or PASSENGER");
        }

        int clamped = clamp(limit, 1, 1000);
        return repo.listUsersWithStatus(r, query, clamped);
    }

    @Transactional
    public AdminUserStatusResponseDto setBlockStatus(long userId, AdminSetUserBlockRequestDto req) {
        if (req == null || req.getBlocked() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "blocked is required");
        }

        String role = userLookupRepository.findRoleById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String r = role == null ? "" : role.trim().toUpperCase();
        if (!"DRIVER".equals(r) && !"PASSENGER".equals(r)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DRIVER and PASSENGER can be blocked");
        }

        boolean blocked = Boolean.TRUE.equals(req.getBlocked());
        String reason = blocked ? trimToNull(req.getBlockReason()) : null;

        int updated = repo.updateBlockStatus(userId, blocked, reason);
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if ("DRIVER".equals(r)) {
            driverRepository.findDriverIdByUserId(userId)
                    .ifPresent(driverId -> {
                        if (blocked) {
                            // blocked drivers must never be available for new assignments
                            driverRepository.setAvailable(driverId, false);
                        } else {
                            boolean hasActiveRide = driverRideRepository.findActiveRideDetails(driverId).isPresent();
                            driverRepository.setAvailable(driverId, !hasActiveRide);
                        }
                    });
        }

        return repo.findUserStatusById(userId)
                .orElseGet(() -> new AdminUserStatusResponseDto(userId, null, null, null, r, blocked, reason));
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
