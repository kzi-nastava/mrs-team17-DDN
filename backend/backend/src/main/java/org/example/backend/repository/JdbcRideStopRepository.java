package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcRideStopRepository implements RideStopRepository {

    private final JdbcClient jdbc;

    public JdbcRideStopRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertStops(Long rideId, List<String> stopAddressesOrdered) {
        if (stopAddressesOrdered == null || stopAddressesOrdered.isEmpty()) return;

        int order = 1;
        for (String addr : stopAddressesOrdered) {
            String a = addr == null ? "" : addr.trim();
            if (a.isEmpty()) continue;

            jdbc.sql("""
                insert into ride_stops (ride_id, stop_order, address)
                values (:rideId, :stopOrder, :address)
            """)
                    .param("rideId", rideId)
                    .param("stopOrder", order++)
                    .param("address", a)
                    .update();
        }
    }
}
