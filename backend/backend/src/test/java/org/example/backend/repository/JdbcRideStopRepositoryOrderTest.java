package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class JdbcRideStopRepositoryOrderTest extends PostgresTestContainerBase {

    private static final long RIDE_ID = 701L;

    @Autowired
    private JdbcRideStopRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void insertStops_shouldNoop_whenNullOrEmpty() {
        repository.insertStops(RIDE_ID, null);
        repository.insertStops(RIDE_ID, List.of());

        long cnt = jdbc.sql("select count(1) from ride_stops")
                .query(Long.class)
                .single();
        assertEquals(0L, cnt);
    }

    @Test
    void insertStops_shouldInsertOnlyNonBlankTrimmedAddresses_inGivenOrder() {
        repository.insertStops(RIDE_ID, List.of(
                new RideStopRepository.StopRow(0, "   ", 45.0, 19.0),
                new RideStopRepository.StopRow(1, "  Stop A  ", 45.1, 19.1),
                new RideStopRepository.StopRow(2, "Stop B", 45.2, 19.2)
        ));

        long cnt = jdbc.sql("select count(1) from ride_stops where ride_id = :rid")
                .param("rid", RIDE_ID)
                .query(Long.class)
                .single();
        assertEquals(2L, cnt);

        String firstAddr = jdbc.sql("select address from ride_stops where ride_id = :rid and stop_order = 1")
                .param("rid", RIDE_ID)
                .query(String.class)
                .single();
        assertEquals("Stop A", firstAddr);

        Double lat = jdbc.sql("select lat from ride_stops where ride_id = :rid and stop_order = 2")
                .param("rid", RIDE_ID)
                .query(Double.class)
                .single();
        assertEquals(45.2, lat);
    }
}
