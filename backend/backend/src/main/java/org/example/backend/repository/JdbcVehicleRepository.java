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
}
