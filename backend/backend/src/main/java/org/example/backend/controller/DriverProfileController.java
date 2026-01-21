package org.example.backend.controller;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.*;
import org.example.backend.repository.DriverProfileRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Locale;

@RestController
@RequestMapping("/api/drivers")
public class DriverProfileController {

    private final DriverProfileRepository repo;

    public DriverProfileController(DriverProfileRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{driverId}/profile")
    public ResponseEntity<DriverProfileResponseDto> getDriverProfile(@PathVariable Long driverId) {

        UserProfileResponseDto driver = repo.findDriverUserProfile(driverId)
                .orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        VehicleInfoResponseDto vehicle = new VehicleInfoResponseDto();
        vehicle.setModel("Skoda Octavia");
        vehicle.setType("standard");
        vehicle.setLicensePlate("NS-123-AB");
        vehicle.setSeats(4);
        vehicle.setBabyTransport(true);
        vehicle.setPetTransport(false);

        int activeMinutes = repo.calcActiveMinutesLast24h(driverId);

        DriverProfileResponseDto response = new DriverProfileResponseDto();
        response.setDriver(driver);
        response.setVehicle(vehicle);
        response.setActiveMinutesLast24h(activeMinutes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{driverId}/profile-change-requests")
    public ResponseEntity<ProfileChangeRequestResponseDto> requestProfileChange(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverProfileRequestDto request) {

        if (!repo.findDriverUserProfile(driverId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long requestId = repo.insertProfileChangeRequest(driverId, request, now);

        ProfileChangeRequestResponseDto response = new ProfileChangeRequestResponseDto();
        response.setRequestId(requestId);
        response.setDriverId(driverId);
        response.setStatus("PENDING");
        response.setCreatedAt(now.toLocalDateTime());

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping(
            value = "/{driverId}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProfileImageUploadResponseDto> uploadProfileImage(
            @PathVariable Long driverId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

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

        String filename = "driver-" + driverId + "-" + System.currentTimeMillis() + "." + ext;
        File target = new File(baseDir, filename);

        Files.copy(file.getInputStream(), target.toPath());

        String url = "/public/profile-images/" + filename;
        return ResponseEntity.ok(new ProfileImageUploadResponseDto(url));
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("webp")) return ext;
        return "";
    }
}
