package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Repository
public class JdbcRideOrderRepository implements RideOrderRepository {

    private final JdbcClient jdbc;

    public JdbcRideOrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertRideReturningId(
            Long driverId,
            OffsetDateTime startedAt,
            String startAddress,
            String destinationAddress,
            BigDecimal price,
            String status,
            double startLat, double startLng,
            double destLat, double destLng,
            double carLat, double carLng
    ) {
        return jdbc.sql("""
            insert into rides (
                driver_id,
                started_at,
                start_address,
                destination_address,
                status,
                price,
                start_lat, start_lng,
                dest_lat, dest_lng,
                car_lat, car_lng
            )
            values (
                :driverId,
                :startedAt,
                :startAddress,
                :destAddress,
                :status,
                :price,
                :startLat, :startLng,
                :destLat, :destLng,
                :carLat, :carLng
            )
            returning id
        """)
                .param("driverId", driverId)
                .param("startedAt", startedAt)
                .param("startAddress", startAddress)
                .param("destAddress", destinationAddress)
                .param("status", status)
                .param("price", price)
                .param("startLat", startLat)
                .param("startLng", startLng)
                .param("destLat", destLat)
                .param("destLng", destLng)
                .param("carLat", carLat)
                .param("carLng", carLng)
                .query(Long.class)
                .single();
    }

    @Override
    public boolean userHasActiveRide(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        Boolean exists = jdbc.sql("""
        select exists (
          select 1
          from rides r
          join ride_passengers rp on rp.ride_id = r.id
          where r.status = 'ACTIVE'
            and r.ended_at is null
            and r.canceled = false
            and lower(rp.email) = lower(:email)
        )
    """)
                .param("email", email.trim())
                .query(Boolean.class)
                .single();

        return Boolean.TRUE.equals(exists);
    }

}
