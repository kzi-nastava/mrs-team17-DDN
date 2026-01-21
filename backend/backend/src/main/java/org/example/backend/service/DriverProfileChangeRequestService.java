package org.example.backend.service;

import org.example.backend.repository.DriverProfileChangeRequestRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class DriverProfileChangeRequestService {

    private final DriverProfileChangeRequestRepository repo;
    private final JdbcClient jdbc;

    public DriverProfileChangeRequestService(DriverProfileChangeRequestRepository repo, JdbcClient jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean approve(Long requestId, Long adminId, String note) {
        DriverProfileChangeRequestRepository.DriverProfileChangeRequestRow r =
                repo.findByIdForUpdate(requestId).orElse(null);

        if (r == null || !"PENDING".equals(r.status)) return false;

        String updateUsersSql = """
            update users u
            set
              first_name = coalesce(:firstName, u.first_name),
              last_name = coalesce(:lastName, u.last_name),
              address = coalesce(:address, u.address),
              phone = coalesce(:phone, u.phone),
              profile_image_url = coalesce(:profileImageUrl, u.profile_image_url),
              updated_at = now()
            from drivers d
            where d.user_id = u.id
              and d.id = :driverId
        """;

        jdbc.sql(updateUsersSql)
                .param("firstName", r.firstName)
                .param("lastName", r.lastName)
                .param("address", r.address)
                .param("phone", r.phone)
                .param("profileImageUrl", r.profileImageUrl)
                .param("driverId", r.driverId)
                .update();

        int updated = repo.markApproved(requestId, adminId, note, OffsetDateTime.now());
        return updated == 1;
    }

    @Transactional
    public boolean reject(Long requestId, Long adminId, String note) {
        DriverProfileChangeRequestRepository.DriverProfileChangeRequestRow r =
                repo.findByIdForUpdate(requestId).orElse(null);

        if (r == null || !"PENDING".equals(r.status)) return false;

        int updated = repo.markRejected(requestId, adminId, note, OffsetDateTime.now());
        return updated == 1;
    }
}
