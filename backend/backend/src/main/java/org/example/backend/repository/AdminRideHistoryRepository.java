package org.example.backend.repository;

import org.example.backend.dto.response.AdminRideDetailsResponseDto;
import org.example.backend.dto.response.AdminRideHistoryItemDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminRideHistoryRepository {

    /** Full ride history, any driver or passenger. */
    List<AdminRideHistoryItemDto> listAll(LocalDate from, LocalDate to, String sort, String dir);

    /** History of rides for one driver (driverUserId = users.id). */
    List<AdminRideHistoryItemDto> listByDriver(long driverUserId, LocalDate from, LocalDate to, String sort, String dir);

    /** History of rides for one passenger (passengerUserId = users.id). */
    List<AdminRideHistoryItemDto> listByPassenger(long passengerUserId, LocalDate from, LocalDate to, String sort, String dir);

    /** Detailed view of a single ride: route, driver, passengers, reports, rating. */
    Optional<AdminRideDetailsResponseDto> findDetails(long rideId);

    /** Everything needed to place a new order using the same route/vehicle as a past ride. */
    Optional<ReorderSource> findReorderSource(long rideId);

    record ReorderStop(String address, double lat, double lng) {}

    record ReorderSource(
            Long passengerUserId,
            String startAddress, double startLat, double startLng,
            String destinationAddress, double destLat, double destLng,
            List<ReorderStop> stops,
            String vehicleType,
            boolean babyTransport,
            boolean petTransport
    ) {}
}
