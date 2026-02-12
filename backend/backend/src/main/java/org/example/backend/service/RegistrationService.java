package org.example.backend.service;

import org.example.backend.dto.request.RegisterRequestDto;
import org.example.backend.dto.response.RegisterResponseDto;
import org.example.backend.repository.RegistrationTokenRepository;
import org.example.backend.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.example.backend.event.RegistrationEmailEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final DriverRepository driverRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    @Value("${app.frontendBaseUrl:http://localhost:4200}")
    private String frontendBaseUrl;

    private static final long TOKEN_TTL_HOURS = 24;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            DriverRepository driverRepository,
            RegistrationTokenRepository registrationTokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events
    ) {
        this.userAccountRepository = userAccountRepository;
        this.driverRepository = driverRepository;
        this.registrationTokenRepository = registrationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        return registerInternal(request, RegistrationRole.PASSENGER);
    }

    @Transactional
    public RegisterResponseDto registerDriver(RegisterRequestDto request) {
        return registerInternal(request, RegistrationRole.DRIVER);
    }

    private RegisterResponseDto registerInternal(RegisterRequestDto request, RegistrationRole role) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String email = request.getEmail();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (isBlank(email)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        if (isBlank(password) || isBlank(confirmPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        if (!password.equals(confirmPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");

        if (userAccountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        String passwordHash = passwordEncoder.encode(password);

        Long userId;
        if (role == RegistrationRole.DRIVER) {
            userId = userAccountRepository.insertDriverUserReturningId(
                    email,
                    passwordHash,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAddress(),
                    request.getPhone()
            );
            driverRepository.insertDriverReturningId(userId);
        } else {
            userId = userAccountRepository.insertPassengerUserReturningId(
                    email,
                    passwordHash,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAddress(),
                    request.getPhone()
            );
        }

        // 3) token + persist (ovde čuvamo userId i token + expires)
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);

        registrationTokenRepository.createToken(userId, token, expiresAt);

        // 4) mail link ka frontu
        String link = frontendBaseUrl + "/registration-confirm?token=" + token;
        events.publishEvent(new RegistrationEmailEvent(email, link));

        return new RegisterResponseDto();
    }

    @Transactional
    public void confirm(String token) {
        if (isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }

        Optional<RegistrationTokenRepository.TokenRow> tokenRowOpt =
                registrationTokenRepository.findValidToken(token, Instant.now());

        RegistrationTokenRepository.TokenRow tokenRow = tokenRowOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token")
        );

        // Pošto je password već upisan pri insert-u, ovde nam treba samo aktivacija.
        // Ali vaš repo nema activateOnly, ima activateAndSetPassword.
        // Rešenje: pozovi activateAndSetPassword sa istim hash-om koji je već u bazi
        // (ali mi ga ovde ne znamo).
        //
        // Zato imamo 2 opcije:
        // A) dodamo novu metodu activateUser(userId) u repo (preporučeno)
        // B) promenimo schema/token da pamti passwordHash i ovde setujemo (OK ali nepotrebno komplikovano)

        int updated = userAccountRepository.activateUser(tokenRow.userId());
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to activate account");
        }

        registrationTokenRepository.markUsed(token, Instant.now());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private enum RegistrationRole {
        PASSENGER,
        DRIVER
    }
}
