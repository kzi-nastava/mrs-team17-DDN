package org.example.backend.repository;

import java.util.List;

public interface RideStopRepository {
    void insertStops(Long rideId, List<String> stopAddressesOrdered);
}
