package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;

import java.util.List;

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
class JdbcRidePassengerRepositoryOrderTest extends PostgresTestContainerBase {

    private static final long RIDE_ID = 501L;

    @Autowired
    private JdbcRidePassengerRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void insertPassengers_shouldNoop_whenNullOrEmpty() {
        repository.insertPassengers(RIDE_ID, null);
        repository.insertPassengers(RIDE_ID, List.of());

        long cnt = jdbc.sql("select count(1) from ride_passengers")
                .query(Long.class)
                .single();

        assertEquals(0L, cnt);
    }

    @Test
    void insertPassengers_shouldInsertTrimmedRows_andNullifyEmptyEmail() {
        repository.insertPassengers(RIDE_ID, List.of(
                new RidePassengerRepository.PassengerRow("  Pera  ", "   "),
                new RidePassengerRepository.PassengerRow("Mika", "  m@test.com ")
        ));

        long cnt = jdbc.sql("select count(1) from ride_passengers where ride_id = :rid")
                .param("rid", RIDE_ID)
                .query(Long.class)
                .single();
        assertEquals(2L, cnt);

        Long nullEmailCount = jdbc.sql("select count(1) from ride_passengers where ride_id = :rid and email is null")
                .param("rid", RIDE_ID)
                .query(Long.class)
                .single();
        assertEquals(1L, nullEmailCount);

        String trimmedEmail = jdbc.sql("select email from ride_passengers where ride_id = :rid and email is not null")
                .param("rid", RIDE_ID)
                .query(String.class)
                .single();
        assertEquals("m@test.com", trimmedEmail);
    }

    @Test
    void insertPassengers_shouldThrowIllegalArgument_whenNameMissingOrBlank() {
        InvalidDataAccessApiUsageException ex = assertThrows(InvalidDataAccessApiUsageException.class, () ->
                repository.insertPassengers(RIDE_ID, List.of(
                        new RidePassengerRepository.PassengerRow("   ", "a@test.com")
                ))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("passenger name is required"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Passenger name is required", ex.getCause().getMessage());

        long cnt = jdbc.sql("select count(1) from ride_passengers")
                .query(Long.class)
                .single();
        assertEquals(0L, cnt);
    }
}
