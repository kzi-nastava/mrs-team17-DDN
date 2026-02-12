package org.example.backend.service;

import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.request.RidePointRequestDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.exception.ActiveRideConflictException;
import org.example.backend.exception.BlockedUserException;
import org.example.backend.exception.NoAvailableDriverException;
import org.example.backend.osrm.OsrmClient;
import org.example.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideOrderServiceTest {

    private static final long USER_ID = 2L;
    private static final long DRIVER_ID = 10L;
    private static final long RIDE_ID = 100L;

    private static final String EMAIL = "p@test.com";
    private static final String FN = "Pera";
    private static final String LN = "Peric";

    private static final String START_ADDR = "Start";
    private static final String DEST_ADDR = "Dest";
    private static final double START_LAT = 45.0;
    private static final double START_LNG = 19.0;
    private static final double DEST_LAT = 45.01;
    private static final double DEST_LNG = 19.01;

    @Mock
    private OsrmClient osrmClient;

    @Mock
    private DriverMatchingRepository driverRepo;

    @Mock
    private RideOrderRepository rideOrderRepo;

    @Mock
    private RideStopRepository rideStopRepo;

    @Mock
    private RidePassengerRepository passengerRepo;

    @Mock
    private UserLookupRepository userLookupRepo;

    @Mock
    private MailService mailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailQueueService mailQueueService;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private RideOrderService service;

    @Test
    void createRide_shouldThrowIllegalArgument_whenOrderTypeInvalid() {
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("invalid");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID, req));

        assertEquals("orderType must be 'now' or 'schedule'" , ex.getMessage());
        verifyNoInteractions(osrmClient, pricingService, userLookupRepo, rideOrderRepo,driverRepo,passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowIllegalArgument_whenVehicleTypeInvalid() {
        CreateRideRequestDto req = baseNowRequest();
        req.setVehicleType("invalid");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID,req));

        assertEquals("vehicleType must be standard/luxury/van" , ex.getMessage());
        verifyNoInteractions(osrmClient, pricingService, userLookupRepo, rideOrderRepo,driverRepo,passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowIllegalArgument_whenScheduledAtMissing() {
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID,req));

        assertEquals("scheduledAt is required for schedule order" , ex.getMessage());
        verifyNoInteractions(osrmClient, pricingService, userLookupRepo, rideOrderRepo, driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowIllegalArgument_whenScheduledAtInPast() {
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(OffsetDateTime.now().minusMinutes(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID,req));

        assertTrue(ex.getMessage().toLowerCase().contains("future"));
        verifyNoInteractions(osrmClient, pricingService, userLookupRepo, rideOrderRepo, driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowIllegalArgument_whenScheduledAtMoreThan5HoursAhead() {
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(OffsetDateTime.now().plusHours(6));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() ->service.createRide(USER_ID, req));

        assertTrue(ex.getMessage().toLowerCase().contains("5 hours"));
        verifyNoInteractions(osrmClient, pricingService, userLookupRepo, rideOrderRepo, driverRepo,passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowIllegalArgument_whenRequesterNotFound() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID, baseNowRequest()));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(osrmClient).routeDriving(anyList());
        verify(pricingService).basePrice("standard");
        verify(userLookupRepo).findById(USER_ID);
        verifyNoInteractions(rideOrderRepo, driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowIllegalArgument_whenRequesterInactive() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(false, false, null)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> service.createRide(USER_ID, baseNowRequest()));

        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
        verifyNoInteractions(rideOrderRepo, driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowBlockedUserException_whenRequesterBlocked() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, true, "spam")));

        BlockedUserException ex = assertThrows(BlockedUserException.class,() -> service.createRide(USER_ID, baseNowRequest()));

        assertEquals("spam", ex.getBlockReason());
        assertTrue(ex.getMessage().toLowerCase().contains("blocked"));
        verifyNoInteractions(rideOrderRepo, driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_now_shouldThrowActiveRideConflict_whenPassengerHasOpenImmediateRide() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(eq(EMAIL))).thenReturn(true);

        assertThrows(ActiveRideConflictException.class,() -> service.createRide(USER_ID, baseNowRequest()));

        verify(rideOrderRepo).userHasOpenImmediateRide(EMAIL);
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_now_shouldThrowActiveRideConflict_whenScheduledRideTooSoon() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(eq(EMAIL))).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(eq(EMAIL), any())).thenReturn(true);

        ActiveRideConflictException ex = assertThrows(ActiveRideConflictException.class,
                () -> service.createRide(USER_ID, baseNowRequest()));

        assertTrue(ex.getMessage().toLowerCase().contains("scheduled ride"));
        verify(rideOrderRepo).userHasScheduledRideBefore(eq(EMAIL), any());
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldCountRequiredSeatsIncludingMissingLinkedUsers() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(anyString())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(anyString(), any())).thenReturn(false);
        CreateRideRequestDto req = baseNowRequest();
        req.setLinkedUsers(List.of("missing@test.com"));
        when(userLookupRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        when(driverRepo.findAvailableDrivers(eq("standard"), eq(true), eq(false), eq(2))).thenReturn(List.of());
        when(driverRepo.findDriversFinishingSoon(eq("standard"), eq(true), eq(false), eq(2), anyInt())).thenReturn(List.of());

        assertThrows(NoAvailableDriverException.class, () -> service.createRide(USER_ID, req));

        verify(driverRepo).findAvailableDrivers("standard", true, false, 2);
        verify(driverRepo).findDriversFinishingSoon("standard", true, false, 2, 10 * 60);
        verifyNoInteractions(passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowIllegalArgument_whenLinkedUserInactive() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(anyString())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(anyString(), any())).thenReturn(false);
        when(userLookupRepo.findByEmail("inactive@test.com")).thenReturn(Optional.of(
                new UserLookupRepository.UserBasic(99L, "inactive@test.com", "I", "User", false, false, null)
        ));

        CreateRideRequestDto req = baseNowRequest();
        req.setLinkedUsers(List.of("inactive@test.com"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createRide(USER_ID, req));
        assertTrue(ex.getMessage().contains("Linked user is not active"));
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldThrowBlockedUserException_whenLinkedUserBlocked() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(anyString())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(anyString(), any())).thenReturn(false);
        when(userLookupRepo.findByEmail("blocked@test.com")).thenReturn(Optional.of(
                new UserLookupRepository.UserBasic(100L, "blocked@test.com", "B", "User", true, true, "spam")
        ));

        CreateRideRequestDto req = baseNowRequest();
        req.setLinkedUsers(List.of("blocked@test.com"));

        BlockedUserException ex = assertThrows(BlockedUserException.class, () -> service.createRide(USER_ID, req));
        assertEquals("spam", ex.getBlockReason());
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_now_shouldInsertRidePassengersAndSendNotifications() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(eq(EMAIL))).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(eq(EMAIL), any())).thenReturn(false);
        when(driverRepo.findAvailableDrivers(eq("standard"), eq(true), eq(false), eq(1))).thenReturn(List.of(new DriverMatchingRepository.CandidateDriver(DRIVER_ID, 45.0, 19.0)));
        when(driverRepo.hasAssignedScheduledRideBefore(eq(DRIVER_ID), any())).thenReturn(false);
        when(driverRepo.tryClaimAvailableDriver(DRIVER_ID)).thenReturn(true);
        when(rideOrderRepo.insertRideReturningId(
                eq(DRIVER_ID),
                isNull(),
                eq(START_ADDR),
                eq(DEST_ADDR),
                any(BigDecimal.class),
                eq("ACCEPTED"),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble()
        )).thenReturn(RIDE_ID);
        SimpleMailMessage msg = messageFor(EMAIL);
        when(mailService.buildRideAcceptedEmail(eq(EMAIL), eq(RIDE_ID), eq(START_ADDR), eq(DEST_ADDR))).thenReturn(msg);

        CreateRideResponseDto resp = service.createRide(USER_ID, baseNowRequest());

        assertEquals(RIDE_ID, resp.getRideId());
        assertEquals(DRIVER_ID, resp.getDriverId());
        assertEquals("ACCEPTED", resp.getStatus());
        assertEquals(new BigDecimal("1400.00"), resp.getPrice());
        ArgumentCaptor<BigDecimal> priceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(rideOrderRepo).insertRideReturningId(
                eq(DRIVER_ID),
                isNull(),
                eq(START_ADDR),
                eq(DEST_ADDR),
                priceCaptor.capture(),
                eq("ACCEPTED"),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble()
        );
        assertEquals(new BigDecimal("1400.00"), priceCaptor.getValue());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RidePassengerRepository.PassengerRow>> paxCaptor = (ArgumentCaptor<List<RidePassengerRepository.PassengerRow>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

        verify(passengerRepo).insertPassengers(eq(RIDE_ID), paxCaptor.capture());
        assertEquals(1, paxCaptor.getValue().size());
        assertEquals(EMAIL, paxCaptor.getValue().get(0).email());
        assertEquals(FN + " " + LN, paxCaptor.getValue().get(0).name());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = (ArgumentCaptor<List<SimpleMailMessage>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

        verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(10_000L));
        assertEquals(1, batchCaptor.getValue().size());
        assertEquals(msg, batchCaptor.getValue().get(0));

        verify(notificationService).notifyRideAccepted(RIDE_ID);
        verifyNoInteractions(rideStopRepo);
    }

    @Test
    void createRide_schedule_shouldInsertScheduledRideAndReturnScheduledStatus() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRideConflictingWithSchedule(eq(EMAIL), any())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideInWindow(eq(EMAIL), any(), any())).thenReturn(false);
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusHours(1);
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(scheduledAt);
        when(driverRepo.findAssignableDriversForScheduledRide(eq("standard"), eq(true), eq(false), eq(1))).thenReturn(List.of(new DriverMatchingRepository.CandidateDriver(DRIVER_ID, 45.0, 19.0)));
        when(driverRepo.hasOpenImmediateRideConflictingWithSchedule(eq(DRIVER_ID), any())).thenReturn(false);
        when(driverRepo.hasScheduledRideInWindow(eq(DRIVER_ID), any(), any())).thenReturn(false);
        when(rideOrderRepo.insertScheduledRideReturningId(
                eq(DRIVER_ID),
                eq(scheduledAt),
                eq(START_ADDR),
                eq(DEST_ADDR),
                any(BigDecimal.class),
                eq("SCHEDULED"),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                any(),
                any(),
                anyDouble(), anyDouble(),
                eq("standard"),
                eq(true),
                eq(false),
                eq(1)
        )).thenReturn(RIDE_ID);

        CreateRideResponseDto resp = service.createRide(USER_ID, req);

        assertEquals(RIDE_ID, resp.getRideId());
        assertEquals(DRIVER_ID, resp.getDriverId());
        assertEquals("SCHEDULED", resp.getStatus());
        assertEquals(new BigDecimal("1400.00"), resp.getPrice());
        verify(passengerRepo).insertPassengers(eq(RIDE_ID), anyList());
        verifyNoInteractions(mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowActiveRideConflict_whenImmediateRideConflicts() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRideConflictingWithSchedule(eq(EMAIL), any())).thenReturn(true);

        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(OffsetDateTime.now().plusHours(1));

        ActiveRideConflictException ex = assertThrows(ActiveRideConflictException.class, () -> service.createRide(USER_ID, req));
        assertTrue(ex.getMessage().toLowerCase().contains("active ride"));
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowActiveRideConflict_whenScheduledRideInWindow() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRideConflictingWithSchedule(eq(EMAIL), any())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideInWindow(eq(EMAIL), any(), any())).thenReturn(true);

        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(OffsetDateTime.now().plusHours(1));

        ActiveRideConflictException ex = assertThrows(ActiveRideConflictException.class, () -> service.createRide(USER_ID, req));
        assertTrue(ex.getMessage().toLowerCase().contains("conflicting"));
        verifyNoInteractions(driverRepo, passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_schedule_shouldThrowNoAvailableDriver_whenNoCandidates() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRideConflictingWithSchedule(eq(EMAIL), any())).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideInWindow(eq(EMAIL), any(), any())).thenReturn(false);
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusHours(1);
        CreateRideRequestDto req = baseNowRequest();
        req.setOrderType("schedule");
        req.setScheduledAt(scheduledAt);
        when(driverRepo.findAssignableDriversForScheduledRide(eq("standard"), eq(true), eq(false), eq(1))).thenReturn(List.of());

        assertThrows(NoAvailableDriverException.class, () -> service.createRide(USER_ID, req));

        verify(driverRepo).findAssignableDriversForScheduledRide("standard", true, false, 1);
        verifyNoInteractions(passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_now_shouldThrowNoAvailableDriver_whenNoAvailableOrFinishingSoon() {
        stubRouteAndBasePrice();
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(eq(EMAIL))).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(eq(EMAIL), any())).thenReturn(false);
        when(driverRepo.findAvailableDrivers(eq("standard"), eq(true), eq(false), eq(1))).thenReturn(List.of());
        when(driverRepo.findDriversFinishingSoon(eq("standard"), eq(true), eq(false), eq(1), anyInt())).thenReturn(List.of());

        assertThrows(NoAvailableDriverException.class, () -> service.createRide(USER_ID, baseNowRequest()));

        verify(driverRepo).findAvailableDrivers("standard", true, false, 1);
        verify(driverRepo).findDriversFinishingSoon("standard", true, false, 1, 10 * 60);
        verifyNoInteractions(passengerRepo, rideStopRepo, mailService, mailQueueService, notificationService);
    }

    @Test
    void createRide_shouldUseFallbackRoute_whenOsrmFails() {
        when(osrmClient.routeDriving(anyList())).thenThrow(new RuntimeException("osrm down"));
        when(pricingService.basePrice("standard")).thenReturn(new BigDecimal("200"));
        when(userLookupRepo.findById(USER_ID)).thenReturn(Optional.of(user(true, false, null)));
        when(rideOrderRepo.userHasOpenImmediateRide(eq(EMAIL))).thenReturn(false);
        when(rideOrderRepo.userHasScheduledRideBefore(eq(EMAIL), any())).thenReturn(false);
        when(driverRepo.findAvailableDrivers(eq("standard"), eq(true), eq(false), eq(1)))
                .thenReturn(List.of(new DriverMatchingRepository.CandidateDriver(DRIVER_ID, 45.0, 19.0)));
        when(driverRepo.tryClaimAvailableDriver(DRIVER_ID)).thenReturn(true);
        when(rideOrderRepo.insertRideReturningId(
                eq(DRIVER_ID),
                isNull(),
                eq(START_ADDR),
                eq(DEST_ADDR),
                any(BigDecimal.class),
                eq("ACCEPTED"),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                anyDouble(), anyDouble()
        )).thenReturn(RIDE_ID);

        CreateRideResponseDto resp = service.createRide(USER_ID, baseNowRequest());

        assertEquals(RIDE_ID, resp.getRideId());
        assertEquals(DRIVER_ID, resp.getDriverId());
        verify(osrmClient).routeDriving(anyList());
    }

    private void stubRouteAndBasePrice() {
        when(osrmClient.routeDriving(anyList())).thenReturn(new OsrmClient.RouteSummary(10_000.0, 600.0));
        when(pricingService.basePrice("standard")).thenReturn(new BigDecimal("200"));
    }

    private static CreateRideRequestDto baseNowRequest() {
        CreateRideRequestDto req = new CreateRideRequestDto();
        req.setOrderType("now");
        req.setVehicleType("standard");
        req.setBabyTransport(true);
        req.setPetTransport(false);

        req.setStart(point(START_ADDR, START_LAT, START_LNG));
        req.setDestination(point(DEST_ADDR, DEST_LAT, DEST_LNG));

        req.setCheckpoints(null);
        req.setLinkedUsers(null);
        return req;
    }

    private static RidePointRequestDto point(String addr, double lat, double lng) {
        RidePointRequestDto p = new RidePointRequestDto();
        p.setAddress(addr);
        p.setLat(lat);
        p.setLng(lng);
        return p;
    }

    private static UserLookupRepository.UserBasic user(boolean active, boolean blocked, String reason) {
        return new UserLookupRepository.UserBasic(
                USER_ID, EMAIL, FN, LN, active, blocked, reason
        );
    }

    private static SimpleMailMessage messageFor(String email) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(email);
        return m;
    }
}
