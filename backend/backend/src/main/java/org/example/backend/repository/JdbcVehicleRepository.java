package org.example.backend.repository;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcVehicleRepository implements VehicleRepository {

    private final JdbcClient jdbc;

    public JdbcVehicleRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ActiveVehicleResponseDto> findActiveVehicles(
            Double minLat,
            Double maxLat,
            Double minLng,
            Double maxLng
    ) {
        String sql = """
            select
                v.id,
                v.latitude,
                v.longitude,
                exists (
                    select 1
                    from rides r
                    where r.driver_id = v.driver_id
                      and r.ended_at is null
                      and r.canceled = false
                      and r.status = 'ACTIVE'
                ) as busy
            from vehicles v
            where v.latitude  between coalesce(:minLat, v.latitude)  and coalesce(:maxLat, v.latitude)
              and v.longitude between coalesce(:minLng, v.longitude) and coalesce(:maxLng, v.longitude)
            order by v.id
        """;

        return jdbc.sql(sql)
                .param("minLat", minLat)
                .param("maxLat", maxLat)
                .param("minLng", minLng)
                .param("maxLng", maxLng)
                .query((rs, rowNum) -> {
                    ActiveVehicleResponseDto dto = new ActiveVehicleResponseDto();
                    dto.setId(rs.getLong("id"));
                    dto.setLatitude(rs.getDouble("latitude"));
                    dto.setLongitude(rs.getDouble("longitude"));
                    dto.setBusy(rs.getBoolean("busy"));
                    return dto;
                })
                .list();
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        Integer x = jdbc.sql("select 1 from vehicles where license_plate = :plate")
                .param("plate", licensePlate)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return x != null;
    }

    @Override
    public Long insertVehicleReturningId(
            Long driverId,
            double latitude,
            double longitude,
            String model,
            String type,
            String licensePlate,
            int seats,
            boolean babyTransport,
            boolean petTransport
    ) {
        String sql = """
            insert into vehicles
                (driver_id, latitude, longitude, model, type, license_plate, seats, baby_transport, pet_transport)
            values
                (:driverId, :lat, :lng, :model, :type, :plate, :seats, :baby, :pet)
            returning id
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .param("lat", latitude)
                .param("lng", longitude)
                .param("model", model)
                .param("type", type)
                .param("plate", licensePlate)
                .param("seats", seats)
                .param("baby", babyTransport)
                .param("pet", petTransport)
                .query(Long.class)
                .single();
    }

}
