package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(
        scripts = {
                "classpath:sql/finish/schema.sql",
                "classpath:sql/finish/reset.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class JdbcDriverRideRepositoryFinishTest extends PostgresTestContainerBase {

    private static final long DRIVER_ID = 10L;
    private static final long OTHER_DRIVER_ID = 20L;
    private static final long RIDE_ID = 100L;

    @Autowired
    private JdbcDriverRideRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void finishRide_shouldReturnTrueAndUpdateRide_whenRideIsActiveAndOwnedByDriver() {
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", false, null, null, null, null, null, 0, false);

        boolean updated = repository.finishRide(DRIVER_ID, RIDE_ID);

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        OffsetDateTime endedAt = jdbc.sql("select ended_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();

        assertTrue(updated);
        assertEquals("COMPLETED", statusValue);
        assertNotNull(endedAt);
    }

    @Test
    void finishRide_shouldReturnFalse_whenRideBelongsToAnotherDriver() {
        insertRide(RIDE_ID, OTHER_DRIVER_ID, "ACTIVE", false, null, null, null, null, null, 0, false);

        boolean updated = repository.finishRide(DRIVER_ID, RIDE_ID);

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        Long endedAtNullCount = jdbc.sql("select count(1) from rides where id = :rideId and ended_at is null")
                .param("rideId", RIDE_ID)
                .query(Long.class)
                .single();

        assertFalse(updated);
        assertEquals("ACTIVE", statusValue);
        assertEquals(1L, endedAtNullCount);
    }

    @Test
    void finishRide_shouldReturnFalse_whenRideAlreadyEnded() {
        insertRide(
                RIDE_ID,
                DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().minusMinutes(1),
                null,
                null,
                null,
                0,
                false
        );

        boolean updated = repository.finishRide(DRIVER_ID, RIDE_ID);

        assertFalse(updated);
    }

    @Test
    void finishRide_shouldReturnFalse_whenRideIsCanceled() {
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", true, null, null, null, null, null, 0, false);

        boolean updated = repository.finishRide(DRIVER_ID, RIDE_ID);

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();

        assertFalse(updated);
        assertEquals("ACTIVE", statusValue);
    }

    @Test
    void finishRide_shouldReturnFalse_whenRideStatusIsNotActive() {
        insertRide(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null, null, null, null, null, 0, false);

        boolean updated = repository.finishRide(DRIVER_ID, RIDE_ID);

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();

        assertFalse(updated);
        assertEquals("ACCEPTED", statusValue);
    }

    @Test
    void startRide_shouldReturnTrueAndSetStartedAt_whenRideIsAccepted() {
        insertRide(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null, null, null, null, null, 0, false);

        boolean updated = repository.startRide(DRIVER_ID, RIDE_ID);

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        OffsetDateTime startedAt = jdbc.sql("select started_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();

        assertTrue(updated);
        assertEquals("ACTIVE", statusValue);
        assertNotNull(startedAt);
    }

    @Test
    void startRide_shouldPreserveExistingStartedAt_whenRideIsAlreadyActive() {
        OffsetDateTime startedAt = OffsetDateTime.of(2026, 2, 8, 10, 15, 0, 0, ZoneOffset.UTC);
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", false, startedAt, null, null, null, null, 0, false);

        boolean updated = repository.startRide(DRIVER_ID, RIDE_ID);

        OffsetDateTime actualStartedAt = jdbc.sql("select started_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();
        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();

        assertTrue(updated);
        assertEquals("ACTIVE", statusValue);
        assertEquals(startedAt.toInstant(), actualStartedAt.toInstant());
    }

    @Test
    void findDriverRides_shouldApplyDateFiltersAndSortDescending() {
        insertRide(
                101L,
                DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.of(2026, 1, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 1, 5, 10, 30, 0, 0, ZoneOffset.UTC),
                null,
                null,
                null,
                400,
                false
        );
        insertRide(
                102L,
                DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.of(2026, 1, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 1, 20, 10, 30, 0, 0, ZoneOffset.UTC),
                null,
                null,
                null,
                800,
                false
        );
        insertRide(
                103L,
                OTHER_DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.of(2026, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                null,
                null,
                null,
                900,
                false
        );

        List<org.example.backend.dto.response.DriverRideHistoryResponseDto> result = repository.findDriverRides(
                DRIVER_ID,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 31)
        );

        assertEquals(1, result.size());
        assertEquals(102L, result.get(0).getRideId());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    @Test
    void findDriverRideDetails_shouldReturnRideWithStopsPassengersAndReports() {
        insertRide(
                RIDE_ID,
                DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.of(2026, 1, 10, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 1, 10, 10, 45, 0, 0, ZoneOffset.UTC),
                null,
                null,
                null,
                1234,
                true
        );
        insertStop(RIDE_ID, 1, "Stop 1");
        insertStop(RIDE_ID, 2, "Stop 2");
        insertPassenger(RIDE_ID, "Passenger A", "a@example.com");
        insertPassenger(RIDE_ID, "Passenger B", "b@example.com");
        insertReport(RIDE_ID, "Older report", OffsetDateTime.of(2026, 1, 10, 10, 5, 0, 0, ZoneOffset.UTC));
        insertReport(RIDE_ID, "Newest report", OffsetDateTime.of(2026, 1, 10, 10, 15, 0, 0, ZoneOffset.UTC));

        var details = repository.findDriverRideDetails(DRIVER_ID, RIDE_ID);

        assertTrue(details.isPresent());
        var dto = details.get();
        assertEquals(RIDE_ID, dto.getRideId());
        assertEquals("COMPLETED", dto.getStatus());
        assertTrue(dto.isPanicTriggered());
        assertEquals(List.of("Stop 1", "Stop 2"), dto.getStops());
        assertEquals(2, dto.getPassengers().size());

        List<String> passengerEmails = dto.getPassengers().stream()
                .map(org.example.backend.dto.response.PassengerInfoResponseDto::getEmail)
                .toList();
        assertTrue(passengerEmails.contains("a@example.com"));
        assertTrue(passengerEmails.contains("b@example.com"));

        assertEquals(2, dto.getReports().size());
        assertEquals("Newest report", dto.getReports().get(0).getDescription());
        assertEquals("Older report", dto.getReports().get(1).getDescription());
    }

    @Test
    void findActiveRideDetails_shouldReturnOnlyActiveOpenRide() {
        insertRide(101L, DRIVER_ID, "COMPLETED", false, null, OffsetDateTime.now(), null, null, null, 0, false);
        insertRide(102L, DRIVER_ID, "ACTIVE", false, OffsetDateTime.now().minusMinutes(5), null, null, null, null, 0, false);

        var active = repository.findActiveRideDetails(DRIVER_ID);

        assertTrue(active.isPresent());
        assertEquals(102L, active.get().getRideId());
        assertEquals("ACTIVE", active.get().getStatus());
        assertEquals(0, active.get().getStops().size());
    }

    @Test
    void findAcceptedRides_shouldSortByScheduledAtThenCreatedAt() {
        insertRide(
                201L,
                DRIVER_ID,
                "ACCEPTED",
                false,
                null,
                null,
                null,
                OffsetDateTime.of(2026, 2, 8, 10, 0, 0, 0, ZoneOffset.UTC),
                null,
                0,
                false
        );
        insertRide(
                202L,
                DRIVER_ID,
                "ACCEPTED",
                false,
                null,
                null,
                OffsetDateTime.of(2026, 2, 8, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 2, 8, 11, 0, 0, 0, ZoneOffset.UTC),
                null,
                0,
                false
        );
        insertRide(
                203L,
                DRIVER_ID,
                "ACCEPTED",
                false,
                null,
                null,
                OffsetDateTime.of(2026, 2, 8, 12, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 2, 8, 8, 0, 0, 0, ZoneOffset.UTC),
                null,
                0,
                false
        );

        List<org.example.backend.dto.response.DriverRideDetailsResponseDto> accepted = repository.findAcceptedRides(DRIVER_ID);

        assertEquals(3, accepted.size());
        assertEquals(202L, accepted.get(0).getRideId());
        assertEquals(201L, accepted.get(1).getRideId());
        assertEquals(203L, accepted.get(2).getRideId());
    }

    @Test
    void hasUpcomingAssignedRide_shouldReturnTrue_whenAssignedRideExists() {
        insertRide(301L, DRIVER_ID, "ACCEPTED", false, null, null, OffsetDateTime.now().plusHours(1), null, null, 0, false);

        boolean hasUpcoming = repository.hasUpcomingAssignedRide(DRIVER_ID);

        assertTrue(hasUpcoming);
    }

    @Test
    void hasUpcomingAssignedRide_shouldReturnTrue_whenScheduledRideIsWithinLockWindow() {
        insertRide(304L, DRIVER_ID, "SCHEDULED", false, null, null, OffsetDateTime.now().plusMinutes(10), null, null, 0, false);

        boolean hasUpcoming = repository.hasUpcomingAssignedRide(DRIVER_ID);

        assertTrue(hasUpcoming);
    }

    @Test
    void hasUpcomingAssignedRide_shouldReturnFalse_whenScheduledRideIsOutsideLockWindow() {
        insertRide(305L, DRIVER_ID, "SCHEDULED", false, null, null, OffsetDateTime.now().plusHours(2), null, null, 0, false);

        boolean hasUpcoming = repository.hasUpcomingAssignedRide(DRIVER_ID);

        assertFalse(hasUpcoming);
    }

    @Test
    void hasUpcomingAssignedRide_shouldReturnFalse_whenNoOpenAssignedRidesExist() {
        insertRide(
                302L,
                DRIVER_ID,
                "COMPLETED",
                false,
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(1),
                null,
                null,
                null,
                0,
                false
        );
        insertRide(
                303L,
                DRIVER_ID,
                "ACCEPTED",
                true,
                null,
                null,
                OffsetDateTime.now().plusHours(2),
                null,
                null,
                0,
                false
        );

        boolean hasUpcoming = repository.hasUpcomingAssignedRide(DRIVER_ID);

        assertFalse(hasUpcoming);
    }

    private void insertRide(
            long rideId,
            long driverId,
            String status,
            boolean canceled,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            OffsetDateTime scheduledAt,
            OffsetDateTime createdAt,
            String canceledBy,
            double price,
            boolean panicTriggered
    ) {
        jdbc.sql("""
                insert into rides (
                    id,
                    driver_id,
                    status,
                    canceled,
                    canceled_by,
                    started_at,
                    ended_at,
                    scheduled_at,
                    created_at,
                    start_address,
                    destination_address,
                    price,
                    panic_triggered
                ) values (
                    :id,
                    :driverId,
                    :status,
                    :canceled,
                    :canceledBy,
                    :startedAt,
                    :endedAt,
                    :scheduledAt,
                    coalesce(:createdAt, now()),
                    'Start',
                    'Destination',
                    :price,
                    :panicTriggered
                )
                """)
                .param("id", rideId)
                .param("driverId", driverId)
                .param("status", status)
                .param("canceled", canceled)
                .param("canceledBy", canceledBy)
                .param("startedAt", startedAt)
                .param("endedAt", endedAt)
                .param("scheduledAt", scheduledAt)
                .param("createdAt", createdAt)
                .param("price", price)
                .param("panicTriggered", panicTriggered)
                .update();
    }

    private void insertStop(long rideId, int order, String address) {
        jdbc.sql("""
                insert into ride_stops (ride_id, stop_order, address)
                values (:rideId, :stopOrder, :address)
                """)
                .param("rideId", rideId)
                .param("stopOrder", order)
                .param("address", address)
                .update();
    }

    private void insertPassenger(long rideId, String name, String email) {
        jdbc.sql("""
                insert into ride_passengers (ride_id, name, email)
                values (:rideId, :name, :email)
                """)
                .param("rideId", rideId)
                .param("name", name)
                .param("email", email)
                .update();
    }

    private void insertReport(long rideId, String description, OffsetDateTime createdAt) {
        jdbc.sql("""
                insert into ride_reports (ride_id, description, created_at)
                values (:rideId, :description, :createdAt)
                """)
                .param("rideId", rideId)
                .param("description", description)
                .param("createdAt", createdAt)
                .update();
    }
}
