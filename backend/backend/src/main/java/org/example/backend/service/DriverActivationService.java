package org.example.backend.service;

import org.example.backend.dto.request.DriverActivateAccountRequestDto;
import org.example.backend.repository.DriverActivationTokenRepository;
import org.example.backend.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverActivationService {

    private final DriverActivationTokenRepository tokenRepo;
    private final UserAccountRepository userRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DriverActivationService(DriverActivationTokenRepository tokenRepo, UserAccountRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void activate(DriverActivateAccountRequestDto req) {

        String token = req.getToken() == null ? null : req.getToken().trim();
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        String pass = req.getPassword();
        String confirm = req.getConfirmPassword();

        if (pass == null || confirm == null) {
            throw new IllegalArgumentException("Password and confirmPassword are required");
        }

        if (!pass.equals(confirm)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        DriverActivationTokenRepository.TokenRow tokenRow = tokenRepo.findValidByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        String hash = encoder.encode(pass);
        int updated = userRepo.activateAndSetPassword(tokenRow.userId(), hash);
        if (updated == 0) {
            throw new IllegalStateException("User not found");
        }

        tokenRepo.markUsed(tokenRow.id());
    }
}
