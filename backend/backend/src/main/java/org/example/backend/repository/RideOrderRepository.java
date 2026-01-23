package org.example.backend.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface RideOrderRepository {

    Long insertRideReturningId(
            Long driverId,
            OffsetDateTime startedAt,
            String startAddress,
            String destinationAddress,
            BigDecimal price,
            String status,
            double startLat, double startLng,
            double destLat, double destLng,
            double carLat, double carLng
    );
}
