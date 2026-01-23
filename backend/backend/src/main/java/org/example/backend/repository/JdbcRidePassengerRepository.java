package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcRidePassengerRepository implements RidePassengerRepository {

    private final JdbcClient jdbc;

    public JdbcRidePassengerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertPassengers(Long rideId, List<PassengerRow> passengers) {
        if (passengers == null || passengers.isEmpty()) return;

        for (PassengerRow p : passengers) {
            String name = p.name() == null ? "" : p.name().trim();
            String email = p.email() == null ? null : p.email().trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Passenger name is required");
            }

            if (email != null && email.isEmpty()) {
                email = null;
            }

            jdbc.sql("""
                insert into ride_passengers (ride_id, name, email)
                values (:rideId, :name, :email)
            """)
                    .param("rideId", rideId)
                    .param("name", name)
                    .param("email", email)
                    .update();
        }
    }
}
