package org.example.backend.repository;

import org.example.backend.dto.response.AdminRideStatusRowDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcAdminRideStatusRepository implements AdminRideStatusRepository {

    private final JdbcClient jdbc;

    public JdbcAdminRideStatusRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AdminRideStatusRowDto> list(String q, int limit) {
        String qq = (q == null) ? "" : q.trim().toLowerCase();

        return jdbc.sql("""
            select
              r.id as ride_id,
              d.id as driver_id,
              u.id as user_id,
              u.email as driver_email,
              u.first_name as driver_first_name,
              u.last_name as driver_last_name,
              r.status,
              r.started_at,
              v.latitude as car_lat,
              v.longitude as car_lng
            from rides r
            join drivers d on d.id = r.driver_id
            left join users u on u.id = d.user_id
            left join vehicles v on v.driver_id = d.id
            where r.status = 'ACTIVE'
              and r.canceled = false
              and r.ended_at is null
              and (
                :q = '' or
                lower(coalesce(u.email,'')) like ('%' || :q || '%') or
                lower(coalesce(u.first_name,'')) like ('%' || :q || '%') or
                lower(coalesce(u.last_name,'')) like ('%' || :q || '%')
              )
            order by r.started_at desc nulls last, r.id desc
            limit :limit
        """)
                .param("q", qq)
                .param("limit", Math.max(1, Math.min(limit, 200)))
                .query((rs, rn) -> new AdminRideStatusRowDto(
                        rs.getLong("ride_id"),
                        rs.getLong("driver_id"),
                        (Long) rs.getObject("user_id", Long.class),
                        rs.getString("driver_email"),
                        rs.getString("driver_first_name"),
                        rs.getString("driver_last_name"),
                        rs.getString("status"),
                        rs.getObject("started_at", java.time.OffsetDateTime.class),
                        rs.getDouble("car_lat"),
                        rs.getDouble("car_lng")
                ))
                .list();
    }
}
