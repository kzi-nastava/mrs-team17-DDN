package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(
        scripts = {
                "classpath:sql/order/schema.sql",
                "classpath:sql/order/reset.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class JdbcRideOrderRepositoryOrderTest extends PostgresTestContainerBase {

    private static final long RIDE_ID = 1001L;
    private static final long DRIVER_ID = 10L;
    private static final String EMAIL = "A@Test.com";

    @Autowired
    private JdbcRideOrderRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void userHasOpenImmediateRide_shouldBeTrue_forAcceptedRideWithPassenger_emailCaseInsensitive() {
        insertRideWithId(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null, null, null, 600.0);
        insertPassenger(RIDE_ID, "P", EMAIL);

        assertTrue(repository.userHasOpenImmediateRide("a@test.com"));
    }

    @Test
    void userHasOpenImmediateRide_shouldBeFalse_whenRideEndedOrCanceledOrWrongStatus() {
        insertRideWithId(RIDE_ID, DRIVER_ID, "COMPLETED", false, OffsetDateTime.now(), null, null, 600.0);
        insertPassenger(RIDE_ID, "P", "p@test.com");
        assertFalse(repository.userHasOpenImmediateRide("p@test.com"));

        insertRideWithId(1002L, DRIVER_ID, "ACCEPTED", true, null, null, null, 600.0);
        insertPassenger(1002L, "P", "c@test.com");
        assertFalse(repository.userHasOpenImmediateRide("c@test.com"));
    }

    @Test
    void userHasScheduledRideBefore_shouldReturnTrue_whenScheduledRideExistsBeforeCutoff() {
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusMinutes(30);
        insertRideWithId(RIDE_ID, DRIVER_ID, "SCHEDULED", false, null, scheduledAt, null, 600.0);
        insertPassenger(RIDE_ID, "P", "s@test.com");

        assertTrue(repository.userHasScheduledRideBefore("s@test.com", OffsetDateTime.now().plusHours(1)));
        assertFalse(repository.userHasScheduledRideBefore("s@test.com", OffsetDateTime.now().plusMinutes(5)));
    }

    @Test
    void userHasScheduledRideInWindow_shouldReturnTrue_whenScheduledAtBetweenBounds() {
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusMinutes(45);
        insertRideWithId(RIDE_ID, DRIVER_ID, "SCHEDULED", false, null, scheduledAt, null, 600.0);
        insertPassenger(RIDE_ID, "P", "w@test.com");

        assertTrue(repository.userHasScheduledRideInWindow(
                "w@test.com",
                OffsetDateTime.now().plusMinutes(30),
                OffsetDateTime.now().plusMinutes(60)
        ));

        assertFalse(repository.userHasScheduledRideInWindow(
                "w@test.com",
                OffsetDateTime.now().plusMinutes(60),
                OffsetDateTime.now().plusMinutes(90)
        ));
    }

    @Test
    void userHasOpenImmediateRideConflictingWithSchedule_shouldRespectDurationEstimate_forAcceptedRide() {
        insertRideWithId(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null, null, null, 7200.0);
        insertPassenger(RIDE_ID, "P", "conf@test.com");

        assertTrue(repository.userHasOpenImmediateRideConflictingWithSchedule(
                "conf@test.com",
                OffsetDateTime.now().plusMinutes(5)
        ));

        assertFalse(repository.userHasOpenImmediateRideConflictingWithSchedule(
                "conf@test.com",
                OffsetDateTime.now().plusHours(5)
        ));
    }

    @Test
    void insertRideReturningId_shouldInsertRideAndReturnGeneratedId() {
        long id = repository.insertRideReturningId(
                DRIVER_ID,
                null,
                "Start",
                "Dest",
                new BigDecimal("560.50"),
                "ACCEPTED",
                45.0, 19.0,
                45.01, 19.01,
                44.9, 18.9,
                10_000.0,
                600.0
        );

        assertTrue(id > 0);

        String status = jdbc.sql("select status from rides where id = :id")
                .param("id", id)
                .query(String.class)
                .single();
        assertEquals("ACCEPTED", status);

        Double dist = jdbc.sql("select est_distance_meters from rides where id = :id")
                .param("id", id)
                .query(Double.class)
                .single();
        assertEquals(10_000.0, dist);
    }

    @Test
    void insertScheduledRideReturningId_shouldPersistRequirementsAndReturnGeneratedId() {
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusHours(1);

        long id = repository.insertScheduledRideReturningId(
                DRIVER_ID,
                scheduledAt,
                "Start",
                "Dest",
                new BigDecimal("1000.00"),
                "SCHEDULED",
                45.0, 19.0,
                45.01, 19.01,
                null, null,
                10_000.0,
                600.0,
                "standard",
                true,
                false,
                3
        );

        Integer seats = jdbc.sql("select required_seats from rides where id = :id")
                .param("id", id)
                .query(Integer.class)
                .single();
        assertEquals(3, seats);

        String vehicleType = jdbc.sql("select vehicle_type from rides where id = :id")
                .param("id", id)
                .query(String.class)
                .single();
        assertEquals("standard", vehicleType);
    }

    private void insertRideWithId(
            long rideId,
            long driverId,
            String status,
            boolean canceled,
            OffsetDateTime endedAt,
            OffsetDateTime scheduledAt,
            OffsetDateTime startedAt,
            Double estDurationSeconds
    ) {
        jdbc.sql("""
                insert into rides (
                    id, driver_id, status, canceled,
                    ended_at, scheduled_at, started_at,
                    est_duration_seconds,
                    dest_lat, dest_lng
                ) values (
                    :id, :driverId, :status, :canceled,
                    :endedAt, :scheduledAt, :startedAt,
                    :dur,
                    45.01, 19.01
                )
                """)
                .param("id", rideId)
                .param("driverId", driverId)
                .param("status", status)
                .param("canceled", canceled)
                .param("endedAt", endedAt)
                .param("scheduledAt", scheduledAt)
                .param("startedAt", startedAt)
                .param("dur", estDurationSeconds)
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
}
