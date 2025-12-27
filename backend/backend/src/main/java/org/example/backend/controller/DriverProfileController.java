package org.example.backend.controller;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.DriverProfileResponseDto;
import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.example.backend.dto.response.VehicleInfoResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/drivers")
public class DriverProfileController {

    @GetMapping("/{driverId}/profile")
    public ResponseEntity<DriverProfileResponseDto> getDriverProfile(@PathVariable Long driverId) {
        UserProfileResponseDto driver = new UserProfileResponseDto();
        driver.setId(driverId);
        driver.setEmail("driver@example.com");
        driver.setFirstName("Test");
        driver.setLastName("Driver");
        driver.setAddress("Ulica Vozaca 12");
        driver.setPhoneNumber("+38160123456");
        driver.setProfileImageUrl("https://example.com/default-driver.png");
        driver.setRole("DRIVER");

        VehicleInfoResponseDto vehicle = new VehicleInfoResponseDto();
        vehicle.setModel("Skoda Octavia");
        vehicle.setType("standard");
        vehicle.setLicensePlate("NS-123-AB");
        vehicle.setSeats(4);
        vehicle.setBabyTransport(true);
        vehicle.setPetTransport(false);

        DriverProfileResponseDto response = new DriverProfileResponseDto();
        response.setDriver(driver);
        response.setVehicle(vehicle);
        response.setActiveMinutesLast24h(137);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{driverId}/profile-change-requests")
    public ResponseEntity<ProfileChangeRequestResponseDto> requestProfileChange(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverProfileRequestDto request) {

        ProfileChangeRequestResponseDto response = new ProfileChangeRequestResponseDto();
        response.setRequestId(1L);
        response.setDriverId(driverId);
        response.setStatus("PENDING");
        response.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.accepted().body(response);
    }
}
