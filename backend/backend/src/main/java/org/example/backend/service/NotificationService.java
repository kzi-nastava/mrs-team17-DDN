package org.example.backend.service;

import org.example.backend.repository.NotificationRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final JdbcClient jdbc;
    private final NotificationRepository repo;

    public NotificationService(JdbcClient jdbc, NotificationRepository repo) {
        this.jdbc = jdbc;
        this.repo = repo;
    }

    public void notifyRideAccepted(long rideId) {
        List<Long> userIds = registeredPassengerUserIdsForRide(rideId);

        String link = "/user/ride-tracking?rideId=" + rideId;

        repo.createForUsers(
                userIds,
                "RIDE_ACCEPTED",
                "Ride accepted",
                "You were added to a ride and a driver has accepted it.",
                link
        );
    }

    public void notifyRideFinished(long rideId) {
        List<Long> userIds = registeredPassengerUserIdsForRide(rideId);

        String link = "/user/ride-tracking?rideId=" + rideId;

        repo.createForUsers(
                userIds,
                "RIDE_FINISHED",
                "Ride finished",
                "Ride has been successfully completed.",
                link
        );
    }

    private List<Long> registeredPassengerUserIdsForRide(long rideId) {
        return jdbc.sql("""
        select distinct u.id
        from ride_passengers rp
        join users u
          on lower(u.email) = lower(rp.email)
        where rp.ride_id = ?
          and u.is_active = true
          and u.blocked = false
    """)
                .param(rideId)
                .query(Long.class)
                .list();
    }

}
