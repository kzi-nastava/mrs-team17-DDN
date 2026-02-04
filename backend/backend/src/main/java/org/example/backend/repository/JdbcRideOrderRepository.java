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
    public boolean userHasActiveRide(String email) {
        Boolean exists = jdbc.sql("""
            select exists (
                select 1
                from rides r
                join ride_passengers rp on rp.ride_id = r.id
                where lower(rp.email) = lower(:email)
                  and r.canceled = false
                  and r.ended_at is null
                  and r.status in ('ACCEPTED', 'ACTIVE', 'SCHEDULED')
            )
        """)
                .param("email", email)
                .query(Boolean.class)
                .single();

        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Long insertRideReturningId(Long driverId, OffsetDateTime scheduledAt, String startAddress, String destinationAddress,
                                      BigDecimal price, String status, double startLat, double startLng, double destLat,
                                      double destLng, double carLat, double carLng, double distanceMeters, double durationSeconds) {
        return jdbc.sql("""
            insert into rides (
                driver_id, scheduled_at,
                start_address, destination_address,
                price, status,
                start_lat, start_lng, dest_lat, dest_lng,
                car_lat, car_lng,
                est_distance_meters, est_duration_seconds,
                canceled
            )
            values (
                :driverId, :scheduledAt,
                :startAddress, :destinationAddress,
                :price, :status,
                :startLat, :startLng, :destLat, :destLng,
                :carLat, :carLng,
                :dist, :dur,
                false
            )
            returning id
        """)
                .param("driverId", driverId)
                .param("scheduledAt", scheduledAt)
                .param("startAddress", startAddress)
                .param("destinationAddress", destinationAddress)
                .param("price", price)
                .param("status", status)
                .param("startLat", startLat)
                .param("startLng", startLng)
                .param("destLat", destLat)
                .param("destLng", destLng)
                .param("carLat", carLat)
                .param("carLng", carLng)
                .param("dist", distanceMeters)
                .param("dur", durationSeconds)
                .query(Long.class)
                .single();
    }

    @Override
    public Long insertScheduledRideReturningId(OffsetDateTime scheduledAt, String startAddress, String destinationAddress,
                                               BigDecimal price, String status, double startLat, double startLng,
                                               double destLat, double destLng, double distanceMeters, double durationSeconds,
                                               String vehicleType, boolean babyTransport, boolean petTransport, int requiredSeats) {

        return jdbc.sql("""
            insert into rides (
                driver_id, scheduled_at,
                start_address, destination_address,
                price, status,
                start_lat, start_lng, dest_lat, dest_lng,
                car_lat, car_lng,
                est_distance_meters, est_duration_seconds,
                canceled,
                vehicle_type, baby_transport, pet_transport, required_seats
            )
            values (
                null, :scheduledAt,
                :startAddress, :destinationAddress,
                :price, :status,
                :startLat, :startLng, :destLat, :destLng,
                null, null,
                :dist, :dur,
                false,
                :vehicleType, :baby, :pet, :seats
            )
            returning id
        """)
                .param("scheduledAt", scheduledAt)
                .param("startAddress", startAddress)
                .param("destinationAddress", destinationAddress)
                .param("price", price)
                .param("status", status)
                .param("startLat", startLat)
                .param("startLng", startLng)
                .param("destLat", destLat)
                .param("destLng", destLng)
                .param("dist", distanceMeters)
                .param("dur", durationSeconds)
                .param("vehicleType", vehicleType)
                .param("baby", babyTransport)
                .param("pet", petTransport)
                .param("seats", requiredSeats)
                .query(Long.class)
                .single();
    }
}
