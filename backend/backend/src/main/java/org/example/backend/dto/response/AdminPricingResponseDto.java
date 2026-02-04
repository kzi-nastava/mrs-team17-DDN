package org.example.backend.dto.response;

import java.math.BigDecimal;

public record AdminPricingResponseDto(
        BigDecimal standard,
        BigDecimal luxury,
        BigDecimal van
) {}
