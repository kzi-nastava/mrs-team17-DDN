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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class DriverControllerFinishWebMvcTest {

    private static final String INVALID_TOKEN = "invalid-token";

    private static final long RIDE_ID = 100L;
    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DriverRideService driverRideService;

    @MockitoBean
    private DriverRepository driverRepository;

    @Test
    void finishRide_shouldReturn403_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jwtService, driverRepository, driverRideService);
    }

    @Test
    void finishRide_shouldReturn403_whenTokenRoleIsPassenger() throws Exception {
        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(authentication(auth(USER_ID, "PASSENGER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jwtService, driverRepository, driverRideService);
    }

    @Test
    void finishRide_shouldReturn403_whenDriverProfileNotFound() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jwtService);
        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verifyNoInteractions(driverRideService);
        verifyNoMoreInteractions(driverRepository);
    }

    @Test
    void finishRide_shouldReturn200_whenDriverTokenAndDriverIdAreValid() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.of(DRIVER_ID));

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isOk());

        verifyNoInteractions(jwtService);
        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verify(driverRideService).finishRide(DRIVER_ID, RIDE_ID);
    }

    @Test
    void finishRide_shouldReturn404_whenServiceThrowsNotFound() throws Exception {
        when(driverRepository.findDriverIdByUserId(USER_ID)).thenReturn(Optional.of(DRIVER_ID));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"))
                .when(driverRideService).finishRide(DRIVER_ID, RIDE_ID);

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .with(authentication(auth(USER_ID, "DRIVER"))))
                .andExpect(status().isNotFound());

        verifyNoInteractions(jwtService);
        verify(driverRepository).findDriverIdByUserId(USER_ID);
        verify(driverRideService).finishRide(DRIVER_ID, RIDE_ID);
    }

    @Test
    void finishRide_shouldReturn403_whenTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(put("/api/driver/rides/{rideId}/finish", RIDE_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN))
                .andExpect(status().isForbidden());

        verify(jwtService).parseToken(INVALID_TOKEN);
        verify(driverRepository, never()).findDriverIdByUserId(USER_ID);
        verifyNoInteractions(driverRideService);
    }

    private Authentication auth(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                Long.toString(userId),
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
