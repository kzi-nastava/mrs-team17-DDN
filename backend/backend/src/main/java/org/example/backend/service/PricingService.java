package org.example.backend.service;

import org.example.backend.dto.request.AdminPricingUpdateRequestDto;
import org.example.backend.dto.response.AdminPricingResponseDto;
import org.example.backend.repository.PricingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {
    private final PricingRepository repo;

    public PricingService(PricingRepository repo) {
        this.repo = repo;
    }

    public AdminPricingResponseDto get() {
        return repo.get();
    }

    public void update(AdminPricingUpdateRequestDto req) {
        repo.update(req);
    }

    public BigDecimal basePrice(String vehicleType) {
        return repo.getBasePrice(vehicleType);
    }
}
