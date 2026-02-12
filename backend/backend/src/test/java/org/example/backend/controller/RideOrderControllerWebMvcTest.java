package org.example.backend.controller;

import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.security.JwtAuthFilter;
import org.example.backend.security.JwtService;
import org.example.backend.security.SecurityConfig;
import org.example.backend.service.RideOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RideOrderController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@ActiveProfiles("test")
class RideOrderControllerWebMvcTest {

    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;
    private static final long RIDE_ID = 100L;
    private static final String INVALID_TOKEN = "invalid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideOrderService rideOrderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createRide_shouldReturn403_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verifyNoInteractions(rideOrderService);
    }

    @Test
    void createRide_shouldReturn403_whenTokenIsInvalid() throws Exception {
        doThrow(new RuntimeException("invalid token"))
                .when(jwtService).parseToken(INVALID_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verify(jwtService).parseToken(INVALID_TOKEN);
        verifyNoInteractions(rideOrderService);
    }

    @Test
    void createRide_shouldReturn403_whenTokenRoleIsNotPassenger() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verifyNoInteractions(rideOrderService);
    }

    @Test
    void createRide_shouldReturn401_whenPrincipalIsNotNumeric() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth("abc", "PASSENGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        verifyNoInteractions(rideOrderService);
    }

    @Test
    void createRide_shouldReturn400_whenBodyIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(rideOrderService);
    }

    @Test
    void createRide_shouldReturn201_whenPassengerAndValidRequest() throws Exception {
        CreateRideResponseDto resp = new CreateRideResponseDto();
        resp.setRideId(RIDE_ID);
        resp.setDriverId(DRIVER_ID);
        resp.setStatus("ACCEPTED");
        resp.setPrice(new BigDecimal("560.50"));

        when(rideOrderService.createRide(eq(USER_ID), any())).thenReturn(resp);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNowRequestJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.rideId").value((int) RIDE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.driverId").value((int) DRIVER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(560.50));

        verify(rideOrderService).createRide(eq(USER_ID), any());
    }

    private static Authentication auth(long userId, String role) {
        return auth(Long.toString(userId), role);
    }

    private static Authentication auth(String principal, String role) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private static String validNowRequestJson() {
        return """
                {
                  "orderType": "now",
                  "start": {"address": "Start", "lat": 45.0, "lng": 19.0},
                  "destination": {"address": "Dest", "lat": 45.01, "lng": 19.01},
                  "vehicleType": "standard",
                  "babyTransport": true,
                  "petTransport": false
                }
                """;
    }
}
