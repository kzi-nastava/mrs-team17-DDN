package org.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AdminPricingUpdateRequestDto(
        @NotNull @DecimalMin("0.0") BigDecimal standard,
        @NotNull @DecimalMin("0.0") BigDecimal luxury,
        @NotNull @DecimalMin("0.0") BigDecimal van
) {}
