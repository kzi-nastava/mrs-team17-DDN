package org.example.backend.controller;

import org.example.backend.osrm.OsrmClient;
import org.example.backend.service.MailQueueService;
import org.example.backend.service.MailService;
import org.example.backend.service.NotificationService;
import org.example.backend.service.PricingService;
import org.example.backend.testsupport.PostgresTestContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
        scripts = {
                "classpath:sql/order/schema.sql",
                "classpath:sql/order/reset.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class RideOrderControllerIntegrationTest extends PostgresTestContainerBase {

    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;
    private static final long RIDE_ID = 100L;
    private static final String EMAIL = "p@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @MockitoBean
    private OsrmClient osrmClient;

    @MockitoBean
    private PricingService pricingService;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private MailQueueService mailQueueService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createRide_now_shouldReturn201_andPersistRide() throws Exception {
        insertUser(USER_ID, EMAIL, true, false, null);
        insertDriver(DRIVER_ID, null, true);
        insertVehicle(DRIVER_ID, "standard", 4, true, false, 45.0, 19.0);

        stubRouteAndBasePrice();
        when(mailService.buildRideAcceptedEmail(anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(new SimpleMailMessage());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType("application/json")
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.rideId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.driverId").value((int) DRIVER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(1400.00));

        Long cnt = jdbc.sql("select count(1) from rides")
                .query(Long.class)
                .single();
        assertEquals(1L, cnt.longValue());
    }

    @Test
    void createRide_schedule_shouldReturn201_andStatusScheduled() throws Exception {
        insertUser(USER_ID, EMAIL, true, false, null);
        insertDriver(DRIVER_ID, null, false);
        insertVehicle(DRIVER_ID, "standard", 4, true, false, 45.0, 19.0);

        stubRouteAndBasePrice();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusHours(1);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType("application/json")
                        .content(validScheduleRequestJson(scheduledAt)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SCHEDULED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.driverId").value((int) DRIVER_ID));

        String status = jdbc.sql("select status from rides")
                .query(String.class)
                .single();
        assertEquals("SCHEDULED", status);
    }

    @Test
    void createRide_shouldReturn409_whenNoAvailableDrivers() throws Exception {
        insertUser(USER_ID, EMAIL, true, false, null);
        stubRouteAndBasePrice();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType("application/json")
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No available drivers for selected criteria"));
    }

    @Test
    void createRide_shouldReturn409_whenPassengerHasActiveRide() throws Exception {
        insertUser(USER_ID, EMAIL, true, false, null);
        insertRide(RIDE_ID, DRIVER_ID, "ACCEPTED", false, null, null, null, 600.0);
        insertPassenger(RIDE_ID, "P", EMAIL);
        stubRouteAndBasePrice();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType("application/json")
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User already has an active ride."));
    }

    @Test
    void createRide_shouldReturn403_whenUserBlocked() throws Exception {
        insertUser(USER_ID, EMAIL, true, true, "spam");
        stubRouteAndBasePrice();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType("application/json")
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockReason").value("spam"));
    }

    private void stubRouteAndBasePrice() {
        when(osrmClient.routeDriving(anyList())).thenReturn(new OsrmClient.RouteSummary(10_000.0, 600.0));
        when(pricingService.basePrice(eq("standard"))).thenReturn(new BigDecimal("200"));
    }

    private static Authentication auth(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                Long.toString(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private static String validNowRequestJson() {
        return """
                {
                  \"orderType\": \"now\",
                  \"start\": {\"address\": \"Start\", \"lat\": 45.0, \"lng\": 19.0},
                  \"destination\": {\"address\": \"Dest\", \"lat\": 45.01, \"lng\": 19.01},
                  \"vehicleType\": \"standard\",
                  \"babyTransport\": true,
                  \"petTransport\": false
                }
                """;
    }

    private static String validScheduleRequestJson(OffsetDateTime scheduledAt) {
        return String.format("""
                {
                  \"orderType\": \"schedule\",
                  \"scheduledAt\": \"%s\",
                  \"start\": {\"address\": \"Start\", \"lat\": 45.0, \"lng\": 19.0},
                  \"destination\": {\"address\": \"Dest\", \"lat\": 45.01, \"lng\": 19.01},
                  \"vehicleType\": \"standard\",
                  \"babyTransport\": true,
                  \"petTransport\": false
                }
                """, scheduledAt);
    }

    private void insertUser(long id, String email, boolean active, boolean blocked, String reason) {
        jdbc.sql("""
                insert into users (id, email, first_name, last_name, role, is_active, blocked, block_reason)
                values (:id, :email, 'F', 'L', 'PASSENGER', :active, :blocked, :reason)
                """)
                .param("id", id)
                .param("email", email)
                .param("active", active)
                .param("blocked", blocked)
                .param("reason", reason)
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
                    45.01, 19.01
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
