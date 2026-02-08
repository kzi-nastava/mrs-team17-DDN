package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.OffsetDateTime;
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
class JdbcRideRepositoryFinishTest {

    private static final long RIDE_ID = 100L;

    @Autowired
    private JdbcRideRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void findPassengerEmails_shouldReturnAllEmailsForRide() {
        insertRide(RIDE_ID, "A", "B");
        insertPassenger(RIDE_ID, "a@test.com");
        insertPassenger(RIDE_ID, "b@test.com");

        List<String> emails = repository.findPassengerEmails(RIDE_ID);

        assertEquals(2, emails.size());
        assertTrue(emails.contains("a@test.com"));
        assertTrue(emails.contains("b@test.com"));
    }

    @Test
    void findPassengerEmails_shouldReturnEmpty_whenNoPassengersExist() {
        insertRide(RIDE_ID, "A", "B");

        List<String> emails = repository.findPassengerEmails(RIDE_ID);

        assertTrue(emails.isEmpty());
    }

    @Test
    void findRideAddresses_shouldReturnAddresses_whenRideExists() {
        insertRide(RIDE_ID, "Start Address", "Destination Address");

        var addresses = repository.findRideAddresses(RIDE_ID);

        assertTrue(addresses.isPresent());
        assertEquals("Start Address", addresses.get().startAddress());
        assertEquals("Destination Address", addresses.get().destinationAddress());
    }

    @Test
    void findRideAddresses_shouldReturnEmpty_whenRideDoesNotExist() {
        var addresses = repository.findRideAddresses(RIDE_ID);

        assertTrue(addresses.isEmpty());
    }

    @Test
    void finishRide_shouldReturnTrueAndUpdateStatusAndEndedAt_whenRideIsOpenAndNotCanceled() {
        insertRide(RIDE_ID, "ACTIVE", false, null, "A", "B");

        boolean updated = repository.finishRide(RIDE_ID);

        String status = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        OffsetDateTime endedAt = jdbc.sql("select ended_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();

        assertTrue(updated);
        assertEquals("COMPLETED", status);
        assertNotNull(endedAt);
    }

    @Test
    void finishRide_shouldReturnFalse_whenRideAlreadyEndedOrCanceled() {
        insertRide(RIDE_ID, "ACTIVE", true, null, "A", "B");

        boolean updated = repository.finishRide(RIDE_ID);

        String status = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        Long stillNullEndedAt = jdbc.sql("select count(1) from rides where id = :rideId and ended_at is null")
                .param("rideId", RIDE_ID)
                .query(Long.class)
                .single();

        assertFalse(updated);
        assertEquals("ACTIVE", status);
        assertEquals(1L, stillNullEndedAt);
    }

    private void insertRide(long rideId, String startAddress, String destinationAddress) {
        insertRide(rideId, "ACTIVE", false, null, startAddress, destinationAddress);
    }

    private void insertRide(
            long rideId,
            String status,
            boolean canceled,
            OffsetDateTime endedAt,
            String startAddress,
            String destinationAddress
    ) {
        jdbc.sql("""
                insert into rides (
                    id,
                    driver_id,
                    status,
                    canceled,
                    ended_at,
                    start_address,
                    destination_address
                ) values (
                    :id,
                    10,
                    :status,
                    :canceled,
                    :endedAt,
                    :startAddress,
                    :destinationAddress
                )
                """)
                .param("id", rideId)
                .param("status", status)
                .param("canceled", canceled)
                .param("endedAt", endedAt)
                .param("startAddress", startAddress)
                .param("destinationAddress", destinationAddress)
                .update();
    }

    private void insertPassenger(long rideId, String email) {
        jdbc.sql("""
                insert into ride_passengers (ride_id, email)
                values (:rideId, :email)
                """)
                .param("rideId", rideId)
                .param("email", email)
                .update();
    }
}
