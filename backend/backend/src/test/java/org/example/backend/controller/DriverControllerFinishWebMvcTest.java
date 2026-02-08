package org.example.backend.controller;

import org.example.backend.service.MailQueueService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class DriverControllerFinishWebMvcTest {

    private static final String INVALID_TOKEN = "invalid-token";

    private static final long RIDE_ID = 100L;
    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;
    private static final long OTHER_DRIVER_ID = 20L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @MockitoBean
    private MailQueueService mailQueueService;

    @Test
    void finishRide_shouldReturn403_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void finishRide_shouldReturn403_whenTokenRoleIsPassenger() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void finishRide_shouldReturn403_whenDriverProfileNotFound() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void finishRide_shouldReturn200_whenDriverTokenAndDriverIdAreValid() throws Exception {
        insertUser(USER_ID, "driver@test.com");
        insertDriver(DRIVER_ID, USER_ID, false);
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", false, null);

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isOk());

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        OffsetDateTime endedAt = jdbc.sql("select ended_at from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(OffsetDateTime.class)
                .single();
        Boolean driverAvailable = jdbc.sql("select available from drivers where id = :driverId")
                .param("driverId", DRIVER_ID)
                .query(Boolean.class)
                .single();

        assertEquals("COMPLETED", statusValue);
        assertNotNull(endedAt);
        assertEquals(Boolean.TRUE, driverAvailable);
    }

    @Test
    void finishRide_shouldCreateNotificationAndQueueEmail_whenRideHasRegisteredPassenger() throws Exception {
        long passengerUserId = 400L;
        String passengerEmail = "passenger-driver-endpoint@test.com";

        insertUser(USER_ID, "driver@test.com");
        insertDriver(DRIVER_ID, USER_ID, false);
        insertUser(passengerUserId, passengerEmail);
        insertRide(RIDE_ID, DRIVER_ID, "ACTIVE", false, null);
        insertPassenger(RIDE_ID, "Passenger One", passengerEmail);

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isOk());

        Long notificationCount = jdbc.sql("""
                select count(1)
                from notifications
                where user_id = :userId
                  and type = 'RIDE_FINISHED'
                """)
                .param("userId", passengerUserId)
                .query(Long.class)
                .single();
        assertEquals(1L, notificationCount);

        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        assertEquals(1, batchCaptor.getValue().size());
        assertEquals(passengerEmail, batchCaptor.getValue().get(0).getTo()[0]);
    }

    @Test
    void finishRide_shouldReturn404_whenServiceThrowsNotFound() throws Exception {
        insertUser(USER_ID, "driver@test.com");
        insertDriver(DRIVER_ID, USER_ID, false);
        insertRide(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null);

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isNotFound());

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        Boolean driverAvailable = jdbc.sql("select available from drivers where id = :driverId")
                .param("driverId", DRIVER_ID)
                .query(Boolean.class)
                .single();
        assertEquals("ACCEPTED", statusValue);
        assertFalse(driverAvailable);
    }

    @Test
    void finishRide_shouldReturn403_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void finishRide_shouldReturn404_whenRideBelongsToAnotherDriver() throws Exception {
        insertUser(USER_ID, "driver@test.com");
        insertDriver(DRIVER_ID, USER_ID, false);
        insertRide(RIDE_ID, OTHER_DRIVER_ID, "ACTIVE", false, null);

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isNotFound());

        String statusValue = jdbc.sql("select status from rides where id = :rideId")
                .param("rideId", RIDE_ID)
                .query(String.class)
                .single();
        assertEquals("ACTIVE", statusValue);
    }

    private Authentication auth(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                Long.toString(userId),
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
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
                    id,
                    driver_id,
                    status,
                    canceled,
                    ended_at,
                    start_address,
                    destination_address
                ) values (
                    :id,
                    :driverId,
                    :status,
                    :canceled,
                    :endedAt,
                    'Start',
                    'Destination'
                )
                """)
                .param("id", rideId)
                .param("driverId", driverId)
                .param("status", status)
                .param("canceled", canceled)
                .param("endedAt", endedAt)
                .update();
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<SimpleMailMessage>> batchCaptor() {
        return (ArgumentCaptor<List<SimpleMailMessage>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
    }
}
