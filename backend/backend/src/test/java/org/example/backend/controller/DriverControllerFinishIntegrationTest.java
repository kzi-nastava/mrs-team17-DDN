package org.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
        scripts = {
                "classpath:sql/finish/schema.sql",
                "classpath:sql/finish/reset.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class DriverControllerFinishIntegrationTest {

    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;
    private static final long RIDE_ID = 100L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void finishRide_shouldFinishRideAndSetDriverAvailable_whenDataIsValid() throws Exception {
        insertDriver(DRIVER_ID, USER_ID, false);
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", false, null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String status = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        OffsetDateTime endedAt = jdbc.sql("select ended_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();
        Boolean available = jdbc.sql("select available from drivers where id = :driverId")
                .param("driverId", DRIVER_ID)
                .query(Boolean.class)
                .single();

        assertEquals("COMPLETED", status);
        assertNotNull(endedAt);
        assertEquals(Boolean.TRUE, available);
    }

    @Test
    void finishRide_shouldReturn404_whenRideCannotBeFinished() throws Exception {
        insertDriver(DRIVER_ID, USER_ID, false);
        insertRide(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        String status = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        Long endedAtIsNull = jdbc.sql("""
                select count(1)
                from rides
                where id = :rideId and ended_at is null
                """)
                .param("rideId", RIDE_ID)
                .query(Long.class)
                .single();
        Boolean available = jdbc.sql("select available from drivers where id = :driverId")
                .param("driverId", DRIVER_ID)
                .query(Boolean.class)
                .single();

        assertEquals("ACCEPTED", status);
        assertEquals(1L, endedAtIsNull);
        assertEquals(Boolean.FALSE, available);
    }

    @Test
    void finishRide_shouldReturn403_whenRoleIsNotDriver() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER"))))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void finishRide_shouldReturn403_whenDriverProfileMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
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

    private void insertRide(long rideId, long driverId, String status, boolean canceled, OffsetDateTime endedAt) {
        jdbc.sql("""
                insert into rides (
                    id, driver_id, status, canceled, ended_at, start_address, destination_address
                ) values (
                    :id, :driverId, :status, :canceled, :endedAt, 'Start', 'Destination'
                )
                """)
                .param("id", rideId)
                .param("driverId", driverId)
                .param("status", status)
                .param("canceled", canceled)
                .param("endedAt", endedAt)
                .update();
    }

    private Authentication auth(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                Long.toString(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
