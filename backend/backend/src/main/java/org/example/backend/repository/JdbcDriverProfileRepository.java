package org.example.backend.repository;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.example.backend.dto.response.VehicleInfoResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcDriverProfileRepository implements DriverProfileRepository{

    private final JdbcClient jdbc;

    public JdbcDriverProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserProfileResponseDto> findDriverUserProfile(Long driverId) {
        String sql = """
            select
                d.id as driver_id,
                u.id as user_id,
                u.email,
                u.first_name,
                u.last_name,
                u.address,
                u.phone,
                u.role,
                u.profile_image_url,
                u.blocked,
                u.block_reason
            from drivers d
            join users u on u.id = d.user_id
            where d.id = :driverId
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .query((rs, rowNum) -> {
                    UserProfileResponseDto dto = new UserProfileResponseDto();
                    dto.setId(rs.getLong("driver_id"));
                    dto.setEmail(rs.getString("email"));
                    dto.setFirstName(rs.getString("first_name"));
                    dto.setLastName(rs.getString("last_name"));
                    dto.setAddress(rs.getString("address"));
                    dto.setPhoneNumber(rs.getString("phone"));
                    dto.setRole(rs.getString("role"));
                    dto.setProfileImageUrl(rs.getString("profile_image_url"));
                    dto.setBlocked(rs.getBoolean("blocked"));
                    dto.setBlockReason(rs.getString("block_reason"));
                    return dto;
                })
                .optional();
    }

    @Override
    public Optional<VehicleInfoResponseDto> findDriverVehicleInfo(Long driverId) {
        String sql = """
            select
                v.model,
                v.type,
                v.license_plate,
                v.seats,
                v.baby_transport,
                v.pet_transport
            from vehicles v
            where v.driver_id = :driverId
            order by v.id
            limit 1
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .query((rs, rowNum) -> {
                    VehicleInfoResponseDto dto = new VehicleInfoResponseDto();
                    dto.setModel(rs.getString("model"));
                    dto.setType(rs.getString("type"));
                    dto.setLicensePlate(rs.getString("license_plate"));
                    dto.setSeats(rs.getInt("seats"));
                    dto.setBabyTransport(rs.getBoolean("baby_transport"));
                    dto.setPetTransport(rs.getBoolean("pet_transport"));
                    return dto;
                })
                .optional();
    }

    @Override
    public int calcActiveMinutesLast24h(Long driverId) {
        String sql = """
            select coalesce(
                sum(
                    extract(epoch from (coalesce(r.ended_at, now()) - r.started_at)) / 60
                )::int
            , 0) as minutes
            from rides r
            where r.driver_id = :driverId
              and r.started_at is not null
              and r.started_at >= now() - interval '24 hour'
              and r.canceled = false
        """;

        Integer minutes = jdbc.sql(sql)
                .param("driverId", driverId)
                .query(Integer.class)
                .optional()
                .orElse(0);

        return Math.max(0, minutes);
    }

    @Override
    public Long insertProfileChangeRequest(Long driverId, UpdateDriverProfileRequestDto req, OffsetDateTime now) {
        String sql = """
            insert into driver_profile_change_requests
                (driver_id, first_name, last_name, address, phone, profile_image_url, status, created_at)
            values
                (:driverId, :firstName, :lastName, :address, :phone, :profileImageUrl, 'PENDING', :createdAt)
            returning id
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .param("firstName", trimToNull(req.getFirstName()))
                .param("lastName", trimToNull(req.getLastName()))
                .param("address", trimToNull(req.getAddress()))
                .param("phone", trimToNull(req.getPhoneNumber()))
                .param("profileImageUrl", trimToNull(req.getProfileImageUrl()))
                .param("createdAt", now)
                .query(Long.class)
                .single();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
