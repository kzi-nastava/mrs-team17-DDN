package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DriverProfileChangeRequestRepository {

    List<DriverProfileChangeRequestRow> findAll(String status);

    Optional<DriverProfileChangeRequestRow> findByIdForUpdate(Long requestId);

    int markApproved(Long requestId, Long adminId, String note, OffsetDateTime decidedAt);

    int markRejected(Long requestId, Long adminId, String note, OffsetDateTime decidedAt);

    class DriverProfileChangeRequestRow {
        public Long id;
        public Long driverId;
        public String firstName;
        public String lastName;
        public String address;
        public String phone;
        public String profileImageUrl;
        public String status;
        public OffsetDateTime createdAt;
    }
}
