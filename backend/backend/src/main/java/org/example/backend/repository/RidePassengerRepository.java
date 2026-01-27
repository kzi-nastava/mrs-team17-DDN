package org.example.backend.repository;

import java.util.List;

public interface RidePassengerRepository {

    record PassengerRow(String name, String email) {}

    void insertPassengers(Long rideId, List<PassengerRow> passengers);
}
