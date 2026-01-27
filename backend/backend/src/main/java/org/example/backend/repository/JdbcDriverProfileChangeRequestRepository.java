package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDriverProfileChangeRequestRepository implements DriverProfileChangeRequestRepository {

    private final JdbcClient jdbc;

    public JdbcDriverProfileChangeRequestRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<DriverProfileChangeRequestRow> findAll(String status) {
        String sql = """
            select id, driver_id, first_name, last_name, address, phone, profile_image_url, status, created_at
            from driver_profile_change_requests
            where (:status is null or status = :status)
            order by created_at desc
        """;

        return jdbc.sql(sql)
                .param("status", status)
                .query((rs, rowNum) -> {
                    DriverProfileChangeRequestRow r = new DriverProfileChangeRequestRow();
                    r.id = rs.getLong("id");
                    r.driverId = rs.getLong("driver_id");
                    r.firstName = rs.getString("first_name");
                    r.lastName = rs.getString("last_name");
                    r.address = rs.getString("address");
                    r.phone = rs.getString("phone");
                    r.profileImageUrl = rs.getString("profile_image_url");
                    r.status = rs.getString("status");
                    r.createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    return r;
                })
                .list();
    }

    @Override
    public Optional<DriverProfileChangeRequestRow> findByIdForUpdate(Long requestId) {
        String sql = """
            select id, driver_id, first_name, last_name, address, phone, profile_image_url, status, created_at
            from driver_profile_change_requests
            where id = :id
            for update
        """;

        return jdbc.sql(sql)
                .param("id", requestId)
                .query((rs, rowNum) -> {
                    DriverProfileChangeRequestRow r = new DriverProfileChangeRequestRow();
                    r.id = rs.getLong("id");
                    r.driverId = rs.getLong("driver_id");
                    r.firstName = rs.getString("first_name");
                    r.lastName = rs.getString("last_name");
                    r.address = rs.getString("address");
                    r.phone = rs.getString("phone");
                    r.profileImageUrl = rs.getString("profile_image_url");
                    r.status = rs.getString("status");
                    r.createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    return r;
                })
                .optional();
    }

    @Override
    public int markApproved(Long requestId, Long adminId, String note, OffsetDateTime decidedAt) {
        String sql = """
            update driver_profile_change_requests
            set status = 'APPROVED',
                decided_at = :decidedAt,
                decided_by = :adminId,
                note = :note
            where id = :id and status = 'PENDING'
        """;

        return jdbc.sql(sql)
                .param("decidedAt", decidedAt)
                .param("adminId", adminId)
                .param("note", note)
                .param("id", requestId)
                .update();
    }

    @Override
    public int markRejected(Long requestId, Long adminId, String note, OffsetDateTime decidedAt) {
        String sql = """
            update driver_profile_change_requests
            set status = 'REJECTED',
                decided_at = :decidedAt,
                decided_by = :adminId,
                note = :note
            where id = :id and status = 'PENDING'
        """;

        return jdbc.sql(sql)
                .param("decidedAt", decidedAt)
                .param("adminId", adminId)
                .param("note", note)
                .param("id", requestId)
                .update();
    }
}
