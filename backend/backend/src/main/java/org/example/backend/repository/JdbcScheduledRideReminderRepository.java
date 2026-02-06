package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcScheduledRideReminderRepository implements ScheduledRideReminderRepository {

    private final JdbcClient jdbc;

    public JdbcScheduledRideReminderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ReminderRideRow> findRidesInWindow(int minutesAhead, int minutesBehind) {
        return jdbc.sql("""
            select r.id as ride_id, r.scheduled_at
            from rides r
            where r.scheduled_at is not null
              and r.canceled = false
              and r.ended_at is null
              and r.started_at is null
              and r.status in ('SCHEDULED', 'ACCEPTED')
              and r.scheduled_at <= now() + (:ahead * interval '1 minute')
              and r.scheduled_at >= now() - (:behind * interval '1 minute')
            order by r.scheduled_at asc
        """)
                .param("ahead", minutesAhead)
                .param("behind", minutesBehind)
                .query((rs, rn) -> new ReminderRideRow(
                        rs.getLong("ride_id"),
                        rs.getObject("scheduled_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }
}
