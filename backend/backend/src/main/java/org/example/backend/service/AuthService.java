package org.example.backend.service;

import org.example.backend.dto.request.LoginRequestDto;
import org.example.backend.dto.response.LoginResponseDto;
import org.example.backend.dto.response.UserAuthResponseDto;
import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.example.backend.repository.DriverRideRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final DriverRepository drivers;
    private final PasswordEncoder passwordEncoder;
    private final org.example.backend.security.JwtService jwt;
    private final DriverRideRepository driverRides;

    public AuthService(
            UserRepository users,
            DriverRepository drivers,
            DriverRideRepository driverRides,
            PasswordEncoder passwordEncoder,
            org.example.backend.security.JwtService jwt
    ) {
        this.users = users;
        this.drivers = drivers;
        this.driverRides = driverRides;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        String email = request.getEmail().trim().toLowerCase();
        String rawPassword = request.getPassword();

        System.out.println("LOGIN CALLED: " + email);

        // 1) find user
        UserAuthResponseDto user = users.findAuthByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        System.out.println("FOUND USER id=" + user.id() + " email=" + user.email() + " role=" + user.role());
        System.out.println("ACTIVE=" + user.isActive() + " BLOCKED=" + user.blocked());
        System.out.println("HASH=" + user.passwordHash());

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        // 2) password check
        boolean matches = passwordEncoder.matches(rawPassword, user.passwordHash());
        System.out.println("MATCHES=" + matches);

        if (!matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Long driverIdClaim = null;

        if ("DRIVER".equalsIgnoreCase(user.role())) {
            driverIdClaim = drivers.findDriverIdByUserId(user.id()).orElse(null);
            if (driverIdClaim != null) {
                boolean hasActiveRide = driverRides.findActiveRideDetails(driverIdClaim).isPresent();
                drivers.setAvailable(driverIdClaim, !user.blocked() && !hasActiveRide);
            }
        }

        String token = jwt.generateToken(user.id(), user.email(), user.role(), driverIdClaim);

        LoginResponseDto resp = new LoginResponseDto();
        resp.setToken(token);
        return resp;
    }
}
