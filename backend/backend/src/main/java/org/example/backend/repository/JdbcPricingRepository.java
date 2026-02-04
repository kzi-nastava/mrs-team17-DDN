package org.example.backend.repository;

import org.example.backend.dto.request.AdminPricingUpdateRequestDto;
import org.example.backend.dto.response.AdminPricingResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class JdbcPricingRepository implements PricingRepository {

    private final JdbcClient jdbc;

    public JdbcPricingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AdminPricingResponseDto get() {
        BigDecimal standard = jdbc.sql("select base_price from pricing where vehicle_type='standard'")
                .query(BigDecimal.class).single();
        BigDecimal luxury = jdbc.sql("select base_price from pricing where vehicle_type='luxury'")
                .query(BigDecimal.class).single();
        BigDecimal van = jdbc.sql("select base_price from pricing where vehicle_type='van'")
                .query(BigDecimal.class).single();

        return new AdminPricingResponseDto(standard, luxury, van);
    }

    @Override
    public void update(AdminPricingUpdateRequestDto req) {
        jdbc.sql("update pricing set base_price=:p, updated_at=now() where vehicle_type='standard'")
                .param("p", req.standard()).update();
        jdbc.sql("update pricing set base_price=:p, updated_at=now() where vehicle_type='luxury'")
                .param("p", req.luxury()).update();
        jdbc.sql("update pricing set base_price=:p, updated_at=now() where vehicle_type='van'")
                .param("p", req.van()).update();
    }

    @Override
    public BigDecimal getBasePrice(String vehicleType) {
        return jdbc.sql("select base_price from pricing where vehicle_type = :t")
                .param("t", vehicleType)
                .query(BigDecimal.class)
                .optional()
                .orElse(BigDecimal.ZERO);
    }
}
