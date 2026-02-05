package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScheduledRideReminderRepository {

    record ReminderRideRow(
            Long rideId,
            OffsetDateTime scheduledAt
    ) {}

    List<ReminderRideRow> findRidesInWindow(int minutesAhead, int minutesBehind);
}
