package org.example.backend.service;

import org.example.backend.dto.request.RegisterRequestDto;
import org.example.backend.dto.response.RegisterResponseDto;
import org.example.backend.repository.RegistrationTokenRepository;
import org.example.backend.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontendBaseUrl:http://localhost:4200}")
    private String frontendBaseUrl;

    private static final long TOKEN_TTL_HOURS = 24;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            RegistrationTokenRepository registrationTokenRepository,
            MailService mailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.registrationTokenRepository = registrationTokenRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponseDto register(RegisterRequestDto request) {
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

        // 1) Kreiraj user-a kao "neaktivan" (password se može setovati odmah ili tek na confirm)
        // Ako želite da password bude aktivan tek nakon potvrde, onda OVDE NE setujemo password u bazi,
        // nego ga čuvamo privremeno (ne preporučujem).
        // Najjednostavnije: upišemo user-a sa hash-om i neaktivan flag, pa confirm samo aktivira.
        // Ali vaš repo nudi activateAndSetPassword, pa ćemo:
        // - upisati user-a sa nekim placeholder passwordom (ili null ako schema dozvoljava),
        // - na confirm setujemo pravi password hash.
        //
        // Da bi ovo bilo čisto, čuvaćemo passwordHash u token tabeli (najbezbednije od "plain").
        // (Ako već imate drugačiji plan, reci pa prilagodim.)

        String passwordHash = passwordEncoder.encode(password);

        // 2) Insert PASSENGER user (moraš dodati ovu metodu u repo, vidi ispod)
        Long userId = userAccountRepository.insertPassengerUserReturningId(
                email,
                passwordHash,
                request.getFirstName(),
                request.getLastName(),
                request.getAddress(),
                request.getPhone()
        );

        // 3) token + persist (ovde čuvamo userId i token + expires)
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);

        registrationTokenRepository.createToken(userId, token, expiresAt);

        // 4) mail link ka frontu
        String link = frontendBaseUrl + "/registration-confirm?token=" + token;
        mailService.sendRegistrationConfirmation(email, link);

        return new RegisterResponseDto();
    }

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
}