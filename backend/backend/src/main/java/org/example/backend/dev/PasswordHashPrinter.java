package org.example.backend.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashPrinter implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashPrinter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String raw = "test123";
        String hash = passwordEncoder.encode(raw);

        System.out.println("========================================");
        System.out.println("RAW:  " + raw);
        System.out.println("HASH: " + hash);
        System.out.println("========================================");
    }
}
