package org.example.backend.repository;

import org.example.backend.dto.request.AdminPricingUpdateRequestDto;
import org.example.backend.dto.response.AdminPricingResponseDto;

import java.math.BigDecimal;

public interface PricingRepository {
    AdminPricingResponseDto get();
    void update(AdminPricingUpdateRequestDto req);

    BigDecimal getBasePrice(String vehicleType);
}
