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
    public void insertStops(Long rideId, List<StopRow> stopsOrdered) {
        if (stopsOrdered == null || stopsOrdered.isEmpty()) return;

        for (StopRow s : stopsOrdered) {
            String addr = s.address() == null ? "" : s.address().trim();
            if (addr.isEmpty()) continue;

            jdbc.sql("""
                insert into ride_stops (ride_id, stop_order, address, lat, lng)
                values (:rideId, :stopOrder, :address, :lat, :lng)
            """)
                    .param("rideId", rideId)
                    .param("stopOrder", s.stopOrder())
                    .param("address", addr)
                    .param("lat", s.lat())
                    .param("lng", s.lng())
                    .update();
        }
    }
}
