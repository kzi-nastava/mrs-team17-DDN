package org.example.backend.controller;

import org.example.backend.repository.DriverRepository;
import org.example.backend.security.JwtAuthFilter;
import org.example.backend.security.JwtService;
import org.example.backend.security.SecurityConfig;
import org.example.backend.service.DriverRideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = DriverController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@ActiveProfiles("test")
class DriverControllerFinishWebMvcTest {

    private static final String INVALID_TOKEN = "invalid-token";

    private static final long RIDE_ID = 100L;
    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverRideService driverRideService;

    @MockitoBean
    private DriverRepository driverRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void finishRide_shouldReturn403_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verifyNoInteractions(driverRideService);
    }

    @Test
    void finishRide_shouldReturn403_whenTokenRoleIsPassenger() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "PASSENGER"))))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verifyNoInteractions(driverRideService);
    }

    @Test
    void finishRide_shouldReturn403_whenDriverProfileNotFound() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verifyNoInteractions(driverRideService);
    }

    @Test
    void finishRide_shouldReturn200_whenDriverTokenAndDriverIdAreValid() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.of(DRIVER_ID));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verify(driverRideService).finishRide(DRIVER_ID, RIDE_ID);
    }

    @Test
    void finishRide_shouldReturn404_whenServiceThrowsNotFound() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.of(DRIVER_ID));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found or cannot be finished"))
                .when(driverRideService).finishRide(DRIVER_ID, RIDE_ID);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verify(driverRideService).finishRide(DRIVER_ID, RIDE_ID);
    }

    @Test
    void finishRide_shouldReturn403_whenTokenIsInvalid() throws Exception {
        doThrow(new RuntimeException("invalid token"))
                .when(jwtService).parseToken(INVALID_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        verify(jwtService).parseToken(INVALID_TOKEN);
        verifyNoInteractions(driverRideService);
    }

    @Test
    void finishRide_shouldReturn404_whenRideBelongsToAnotherDriver() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.of(DRIVER_ID));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found or cannot be finished"))
                .when(driverRideService).finishRide(DRIVER_ID, RIDE_ID);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verify(driverRideService).finishRide(DRIVER_ID, RIDE_ID);
    }

    private Authentication auth(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                Long.toString(userId),
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
