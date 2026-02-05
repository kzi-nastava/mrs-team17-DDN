package org.example.backend.controller;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.DriverProfileResponseDto;
import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.example.backend.dto.response.ProfileImageUploadResponseDto;
import org.example.backend.repository.DriverRepository;
import org.example.backend.service.DriverProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

@RestController
@RequestMapping("/api/drivers")
public class DriverProfileController {

    private final DriverProfileService service;
    private final DriverRepository driverRepository;

    public DriverProfileController(DriverProfileService service, DriverRepository driverRepository) {
        this.service = service;
        this.driverRepository = driverRepository;
    }

    @GetMapping("/{driverId}/profile")
    public ResponseEntity<DriverProfileResponseDto> getDriverProfile(@PathVariable Long driverId) {
        Authentication auth = requireAuth();

        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
        boolean isDriver = hasRole(auth, "ROLE_DRIVER");

        if (!isAdmin && !isDriver) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (isDriver && !isAdmin) {
            long currentDriverId = requireCurrentDriverId();
            enforceSameDriver(driverId, currentDriverId);
        }

        return service.getProfile(driverId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{driverId}/profile-change-requests")
    public ResponseEntity<ProfileChangeRequestResponseDto> requestProfileChange(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverProfileRequestDto request
    ) {
        long currentDriverId = requireCurrentDriverId();
        enforceSameDriver(driverId, currentDriverId);

        return service.createProfileChangeRequest(currentDriverId, request)
                .map(resp -> ResponseEntity.accepted().body(resp))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/{driverId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponseDto> uploadProfileImage(
            @PathVariable Long driverId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        long currentDriverId = requireCurrentDriverId();
        enforceSameDriver(driverId, currentDriverId);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ProfileImageUploadResponseDto(null));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return ResponseEntity.badRequest().body(new ProfileImageUploadResponseDto(null));
        }

        File baseDir = new File("uploads/profile-images");
        if (!baseDir.exists()) baseDir.mkdirs();

        String original = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String ext = getExtension(original);
        if (ext.isEmpty()) ext = "png";

        String filename = "driver-" + currentDriverId + "-" + System.currentTimeMillis() + "." + ext;
        File target = new File(baseDir, filename);

        Files.copy(file.getInputStream(), target.toPath());

        String url = "/public/profile-images/" + filename;
        return ResponseEntity.ok(new ProfileImageUploadResponseDto(url));
    }

    private void enforceSameDriver(Long pathDriverId, long currentDriverId) {
        if (pathDriverId == null || pathDriverId.longValue() != currentDriverId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own driver profile.");
        }
    }

    private Authentication requireAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private long requireCurrentDriverId() {
        Authentication auth = requireAuth();

        boolean isDriver = hasRole(auth, "ROLE_DRIVER");
        if (!isDriver) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only drivers can access this endpoint");
        }

        long userId;
        try {
            userId = Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        return driverRepository.findDriverIdByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver profile not found"));
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("webp")) return ext;
        return "";
    }
}
