package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
class JdbcDriverMatchingRepositoryOrderTest extends PostgresTestContainerBase{

    private static final long U1 = 1001L;
    private static final long U2 = 1002L;
    private static final long U3 = 1003L;
    private static final long U4 = 1004L;

    private static final long D1 = 11L;
    private static final long D2 = 12L;
    private static final long D3 = 13L;
    private static final long D4 = 14L;
    private static final long D5 = 15L;
    private static final long D6 = 16L;
    private static final long D7 = 17L;
    private static final long D8 = 18L;
    private static final long D9 = 19L;
    private static final long D10 = 20L;

    @Autowired
    private JdbcDriverMatchingRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void findAvailableDrivers_shouldFilterByAvailabilityActiveRideBlockedUserAndWorkLimit(){
        insertUser(U1, "u1@test.com", true, false);
        insertDriver(D1, U1, true);
        insertVehicle(D1, "standard", 4, true, false, 45.0, 19.0);

        insertUser(U2, "u2@test.com", true, false);
        insertDriver(D2, U2, true);
        insertVehicle(D2, "standard", 4, true, false, 45.1, 19.1);
        insertRide(2001L, D2, "ACTIVE", false, OffsetDateTime.now().minusMinutes(2), null, null, 600.0);

        insertUser(U3, "u3@test.com", true, true);
        insertDriver(D3, U3, true);
        insertVehicle(D3, "standard", 4, true, false, 45.2, 19.2);

        insertUser(U4, "u4@test.com", true, false);
        insertDriver(D4, U4, true);
        insertVehicle(D4, "standard", 4, true, false, 45.3, 19.3);
        OffsetDateTime started = OffsetDateTime.now().minusHours(10);
        OffsetDateTime ended = OffsetDateTime.now().minusHours(1);
        insertRide(2002L, D4, "COMPLETED", false, started, ended, null, 100.0);

        insertDriver(D5, null, false);
        insertVehicle(D5, "standard", 4, true, false, 45.4, 19.4);

        List<DriverMatchingRepository.CandidateDriver> res = repository.findAvailableDrivers(
                "standard",
                true,
                false,
                2
        );

        assertEquals(1, res.size());
        assertEquals(D1, res.get(0).driverId());
    }

    @Test
    void findAssignableDriversForScheduledRide_shouldNotRequireAvailableAndShouldNotExcludeActiveRide() {
        insertUser(U1, "u1@test.com", true, false);
        insertDriver(D1, U1, true);
        insertVehicle(D1, "standard", 4, true, false, 45.0, 19.0);

        insertUser(U2, "u2@test.com", true, false);
        insertDriver(D2, U2, true);
        insertVehicle(D2, "standard", 4, true, false, 45.1, 19.1);
        insertRide(3001L, D2, "ACTIVE", false, OffsetDateTime.now().minusMinutes(2), null, null, 600.0);

        insertDriver(D5, null, false);
        insertVehicle(D5, "standard", 4, true, false, 45.4, 19.4);

        Set<Long> ids = repository.findAssignableDriversForScheduledRide("standard", true, false, 2)
                .stream()
                .map(DriverMatchingRepository.CandidateDriver::driverId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(D1, D2, D5), ids);
    }

    @Test
    void findDriversFinishingSoon_shouldReturnOnlyThoseWithinThreshold_andWithoutAcceptedRide() {
        insertDriver(D6, null, true);
        insertVehicle(D6, "standard", 4, false, false, 46.0, 20.0);
        insertRide(4001L, D6, "ACTIVE", false,
                OffsetDateTime.now().minusSeconds(900),
                null,
                null,
                1000.0,
                45.11, 19.11
        );

        insertDriver(D7, null, true);
        insertVehicle(D7, "standard", 4, false, false, 46.1, 20.1);
        insertRide(4002L, D7, "ACTIVE", false,
                OffsetDateTime.now().minusSeconds(1500),
                null,
                null,
                2000.0,
                45.12, 19.12
        );

        insertDriver(D8, null, true);
        insertVehicle(D8, "standard", 4, false, false, 46.2, 20.2);
        insertRide(4003L, D8, "ACTIVE", false,
                OffsetDateTime.now().minusSeconds(900),
                null,
                null,
                1000.0,
                45.13, 19.13
        );
        insertRide(4004L, D8, "ACCEPTED", false,
                null,
                null,
                null,
                600.0,
                45.14, 19.14
        );

        List<DriverMatchingRepository.FinishingSoonDriver> res = repository.findDriversFinishingSoon(
                "standard",
                false,
                false,
                1,
                200
        );

        assertEquals(1, res.size());
        assertEquals(D6, res.get(0).driverId());
        assertTrue(res.get(0).remainingSeconds() >= 0);
        assertTrue(res.get(0).remainingSeconds() <= 200);
    }

    @Test
    void setDriverAvailable_shouldUpdateAndReturnTrue_whenDriverExists() {
        insertDriver(D9, null, false);

        assertTrue(repository.setDriverAvailable(D9, true));

        Boolean available = jdbc.sql("select available from drivers where id = :id")
                .param("id", D9)
                .query(Boolean.class)
                .single();
        assertTrue(available);
    }

    @Test
    void tryClaimAvailableDriver_shouldClaimOnlyOnce() {
        insertDriver(D10, null, true);

        assertTrue(repository.tryClaimAvailableDriver(D10));
        assertFalse(repository.tryClaimAvailableDriver(D10));

        Boolean available = jdbc.sql("select available from drivers where id = :id")
                .param("id", D10)
                .query(Boolean.class)
                .single();
        assertFalse(available);
    }

    @Test
    void scheduleConflictChecks_shouldWorkForDriverId() {
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusMinutes(45);
        insertRide(5001L, D1, "SCHEDULED", false, null, null, scheduledAt, 600.0);

        assertTrue(repository.hasAssignedScheduledRideBefore(D1, OffsetDateTime.now().plusHours(1)));
        assertFalse(repository.hasAssignedScheduledRideBefore(D1, OffsetDateTime.now().plusMinutes(10)));

        assertTrue(repository.hasScheduledRideInWindow(
                D1,
                OffsetDateTime.now().plusMinutes(30),
                OffsetDateTime.now().plusMinutes(60)
        ));
    }

    @Test
    void hasOpenImmediateRideConflictingWithSchedule_shouldRespectDurationEstimate_forAcceptedRide() {
        insertRide(6001L, D1, "ACCEPTED", false, null, null, null, 7200.0);

        assertTrue(repository.hasOpenImmediateRideConflictingWithSchedule(D1, OffsetDateTime.now().plusMinutes(5)));
        assertFalse(repository.hasOpenImmediateRideConflictingWithSchedule(D1, OffsetDateTime.now().plusHours(5)));
    }


    private void insertUser(long id, String email, boolean active, boolean blocked) {
        jdbc.sql("""
                insert into users (id, email, first_name, last_name, role, is_active, blocked, block_reason)
                values (:id, :email, 'F', 'L', 'DRIVER', :active, :blocked, null)
                """)
                .param("id", id)
                .param("email", email)
                .param("active", active)
                .param("blocked", blocked)
                .update();
    }

    private void insertDriver(long driverId, Long userId, boolean available) {
        jdbc.sql("""
                insert into drivers (id, user_id, available)
                values (:id, :uid, :available)
                """)
                .param("id", driverId)
                .param("uid", userId)
                .param("available", available)
                .update();
    }

    private void insertVehicle(
            long driverId,
            String type,
            int seats,
            boolean baby,
            boolean pet,
            double lat,
            double lng
    ) {
        jdbc.sql("""
                insert into vehicles (driver_id, type, seats, baby_transport, pet_transport, latitude, longitude)
                values (:driverId, :type, :seats, :baby, :pet, :lat, :lng)
                """)
                .param("driverId", driverId)
                .param("type", type)
                .param("seats", seats)
                .param("baby", baby)
                .param("pet", pet)
                .param("lat", lat)
                .param("lng", lng)
                .update();
    }

    private void insertRide(
            long rideId,
            long driverId,
            String status,
            boolean canceled,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            OffsetDateTime scheduledAt,
            Double estDurationSeconds
    ) {
        insertRide(rideId, driverId, status, canceled, startedAt, endedAt, scheduledAt, estDurationSeconds, 45.01, 19.01);
    }

    private void insertRide(
            long rideId,
            long driverId,
            String status,
            boolean canceled,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            OffsetDateTime scheduledAt,
            Double estDurationSeconds,
            double destLat,
            double destLng
    ) {
        jdbc.sql("""
                insert into rides (
                    id, driver_id, status, canceled,
                    started_at, ended_at, scheduled_at,
                    est_duration_seconds,
                    dest_lat, dest_lng
                ) values (
                    :id, :driverId, :status, :canceled,
                    :startedAt, :endedAt, :scheduledAt,
                    :dur,
                    :destLat, :destLng
                )
                """)
                .param("id", rideId)
                .param("driverId", driverId)
                .param("status", status)
                .param("canceled", canceled)
                .param("startedAt", startedAt)
                .param("endedAt", endedAt)
                .param("scheduledAt", scheduledAt)
                .param("dur", estDurationSeconds)
                .param("destLat", destLat)
                .param("destLng", destLng)
                .update();
    }
}
