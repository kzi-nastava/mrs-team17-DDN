package org.example.backend.service;

import org.example.backend.dto.request.AdminCreateDriverRequestDto;
import org.example.backend.dto.response.AdminCreateDriverResponseDto;
import org.example.backend.repository.DriverActivationTokenRepository;
import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.UserAccountRepository;
import org.example.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdminDriverService {

    private final UserAccountRepository userRepo;
    private final DriverRepository driverRepo;
    private final VehicleRepository vehicleRepo;
    private final DriverActivationTokenRepository tokenRepo;
    private final MailService mailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.frontendBaseUrl:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${app.driverActivation.validHours:24}")
    private int validHours;

    public AdminDriverService(
            UserAccountRepository userRepo,
            DriverRepository driverRepo,
            VehicleRepository vehicleRepo,
            DriverActivationTokenRepository tokenRepo,
            MailService mailService
    ) {
        this.userRepo = userRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
        this.tokenRepo = tokenRepo;
        this.mailService = mailService;
    }

    @Transactional
    public AdminCreateDriverResponseDto createDriver(AdminCreateDriverRequestDto req) {

        String email = safeTrimLower(req.getEmail());
        if (isBlank(email)) throw new IllegalArgumentException("Email is required");
        if (userRepo.existsByEmail(email)) throw new IllegalArgumentException("Email already exists");

        String plate = safeTrimUpper(req.getLicensePlate());
        if (isBlank(plate)) throw new IllegalArgumentException("License plate is required");
        if (vehicleRepo.existsByLicensePlate(plate)) throw new IllegalArgumentException("License plate already exists");

        String type = normalizeVehicleType(safeTrimLower(req.getVehicleType()));
        if (type == null) throw new IllegalArgumentException("Vehicle type must be standard|luxury|van");

        Integer seats = req.getSeats();
        if (seats == null || seats.intValue() <= 0) throw new IllegalArgumentException("Seats must be > 0");

        Boolean baby = req.getBabyTransport();
        Boolean pet = req.getPetTransport();
        if (baby == null || pet == null) throw new IllegalArgumentException("babyTransport and petTransport are required");

        String placeholderHash = encoder.encode(UUID.randomUUID().toString());

        Long userId = userRepo.insertDriverUserReturningId(
                email,
                placeholderHash,
                safeTrim(req.getFirstName()),
                safeTrim(req.getLastName()),
                safeTrim(req.getAddress()),
                safeTrim(req.getPhoneNumber())
        );

        Long driverId = driverRepo.insertDriverReturningId(userId);

        double[] coords = randomNoviSadCoords();
        vehicleRepo.insertVehicleReturningId(
                driverId,
                coords[0],
                coords[1],
                safeTrim(req.getVehicleModel()),
                type,
                plate,
                seats.intValue(),
                baby.booleanValue(),
                pet.booleanValue()
        );

        String token = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(validHours);
        tokenRepo.createToken(userId, token, expiresAt);

        String activationLink = frontendBaseUrl + "/driver/activate?token=" + token;
        mailService.sendDriverActivationEmail(email, activationLink);

        AdminCreateDriverResponseDto resp = new AdminCreateDriverResponseDto();
        resp.setDriverId(driverId);
        resp.setEmail(email);
        resp.setStatus("PENDING_PASSWORD_SETUP");
        resp.setActivationLinkValidHours(validHours);
        return resp;
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static String safeTrimLower(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String safeTrimUpper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String normalizeVehicleType(String type) {
        if (type == null) return null;
        if ("standard".equals(type)) return "standard";
        if ("luxury".equals(type)) return "luxury";
        if ("van".equals(type)) return "van";
        return null;
    }

    private static double[] randomNoviSadCoords() {
        double minLat = 45.2350, maxLat = 45.3150;
        double minLng = 19.7700, maxLng = 19.9100;
        double lat = ThreadLocalRandom.current().nextDouble(minLat, maxLat);
        double lng = ThreadLocalRandom.current().nextDouble(minLng, maxLng);
        return new double[]{lat, lng};
    }
}
