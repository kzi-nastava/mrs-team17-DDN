package org.example.backend.repository;

import java.util.List;

public interface RideStopRepository {

    record StopRow(int stopOrder, String address, double lat, double lng) {}

    void insertStops(Long rideId, List<StopRow> stopsOrdered);
}
