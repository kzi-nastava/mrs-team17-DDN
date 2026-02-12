package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class JdbcDriverRepositoryFinishTest extends PostgresTestContainerBase {

    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;

    @Autowired
    private JdbcDriverRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void insertDriverReturningId_shouldInsertAndReturnNewDriverId() {
        insertUser(USER_ID, "driver.user@test.local");

        Long insertedId = repository.insertDriverReturningId(USER_ID);

        Long count = jdbc.sql("""
                select count(1)
                from drivers
                where id = :id and user_id = :userId
                """)
                .param("id", insertedId)
                .param("userId", USER_ID)
                .query(Long.class)
                .single();

        assertTrue(insertedId > 0);
        assertEquals(1L, count);
    }

    @Test
    void findDriverIdByUserId_shouldReturnDriverId_whenDriverExists() {
        insertDriver(DRIVER_ID, USER_ID, false);

        var driverId = repository.findDriverIdByUserId(USER_ID);

        assertTrue(driverId.isPresent());
        assertEquals(DRIVER_ID, driverId.get());
    }

    @Test
    void findDriverIdByUserId_shouldReturnEmpty_whenDriverDoesNotExist() {
        var driverId = repository.findDriverIdByUserId(USER_ID);

        assertTrue(driverId.isEmpty());
    }

    @Test
    void setAvailable_shouldUpdateDriverAvailability() {
        insertDriver(DRIVER_ID, USER_ID, false);

        repository.setAvailable(DRIVER_ID, true);

        Boolean available = jdbc.sql("select available from drivers where id = :id")
                .param("id", DRIVER_ID)
                .query(Boolean.class)
                .single();
        assertEquals(Boolean.TRUE, available);
    }

    @Test
    void setAvailable_shouldNotChangeOtherDrivers_whenDriverIdDoesNotExist() {
        insertDriver(DRIVER_ID, USER_ID, false);

        repository.setAvailable(999L, true);

        Boolean available = jdbc.sql("select available from drivers where id = :id")
                .param("id", DRIVER_ID)
                .query(Boolean.class)
                .single();
        assertEquals(Boolean.FALSE, available);
    }

    private void insertDriver(long driverId, long userId, boolean available) {
        jdbc.sql("""
                insert into drivers (id, user_id, available)
                values (:id, :userId, :available)
                """)
                .param("id", driverId)
                .param("userId", userId)
                .param("available", available)
                .update();
    }

    private void insertUser(long userId, String email) {
        jdbc.sql("""
                insert into users (id, email, is_active, blocked)
                values (:id, :email, true, false)
                """)
                .param("id", userId)
                .param("email", email)
                .update();
    }
}
