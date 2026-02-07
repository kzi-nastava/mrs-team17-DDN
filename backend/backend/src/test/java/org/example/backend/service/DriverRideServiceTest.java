package org.example.backend.service;

import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.DriverRideRepository;
import org.example.backend.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(DriverRideService.class)
class DriverRideServiceTest {

    private static final Long DRIVER_ID = 10L;
    private static final Long RIDE_ID = 100L;
    private static final String START_ADDRESS = "A";
    private static final String DEST_ADDRESS = "B";

    @MockitoBean
    private DriverRideRepository repository;

    @MockitoBean
    private DriverRepository driverRepository;

    @MockitoBean
    private RideRepository rideRepository;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private MailQueueService mailQueueService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private DriverRideService service;

    @Test
    void finishRide_shouldThrowNotFound_whenRepositoryReturnsFalse() {
        when(repository.finishRide(DRIVER_ID, RIDE_ID)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.finishRide(DRIVER_ID, RIDE_ID)
        );

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
        verify(repository).finishRide(DRIVER_ID, RIDE_ID);
    }

    @Test
    void finishRide_shouldSetDriverAvailableTrue_whenFinishSucceeds() {
        stubSuccess(List.of(), Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));

        service.finishRide(DRIVER_ID, RIDE_ID);

        verify(driverRepository).setAvailable(DRIVER_ID, true);

        InOrder inOrder = inOrder(
                repository, driverRepository, rideRepository, mailService, mailQueueService, notificationService
        );
        inOrder.verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        inOrder.verify(driverRepository).setAvailable(DRIVER_ID, true);
        inOrder.verify(rideRepository).findPassengerEmails(RIDE_ID);
        inOrder.verify(rideRepository).findRideAddresses(RIDE_ID);
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        inOrder.verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        inOrder.verify(notificationService).notifyRideFinished(RIDE_ID);
        inOrder.verifyNoMoreInteractions();

        assertEquals(0, batchCaptor.getValue().size());
        verifyNoInteractions(mailService);
    }

    @Test
    void finishRide_shouldBuildAndQueueEmails_forNonBlankPassengerEmails() {
        String email1 = "a@test.com";
        String email2 = "b@test.com";
        SimpleMailMessage msg1 = messageFor(email1);
        SimpleMailMessage msg2 = messageFor(email2);

        stubSuccess(List.of(email1, email2), Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));
        when(mailService.buildRideFinishedEmail(email1, RIDE_ID, START_ADDRESS, DEST_ADDRESS)).thenReturn(msg1);
        when(mailService.buildRideFinishedEmail(email2, RIDE_ID, START_ADDRESS, DEST_ADDRESS)).thenReturn(msg2);

        service.finishRide(DRIVER_ID, RIDE_ID);

        InOrder inOrder = inOrder(
                repository, driverRepository, rideRepository, mailService, mailQueueService, notificationService
        );
        inOrder.verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        inOrder.verify(driverRepository).setAvailable(DRIVER_ID, true);
        inOrder.verify(rideRepository).findPassengerEmails(RIDE_ID);
        inOrder.verify(rideRepository).findRideAddresses(RIDE_ID);
        inOrder.verify(mailService).buildRideFinishedEmail(email1, RIDE_ID, START_ADDRESS, DEST_ADDRESS);
        inOrder.verify(mailService).buildRideFinishedEmail(email2, RIDE_ID, START_ADDRESS, DEST_ADDRESS);
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        inOrder.verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        inOrder.verify(notificationService).notifyRideFinished(RIDE_ID);
        inOrder.verifyNoMoreInteractions();

        assertEquals(List.of(msg1, msg2), batchCaptor.getValue());
    }

    @Test
    void finishRide_shouldIgnoreNullAndBlankPassengerEmails() {
        String validEmail = "valid@test.com";
        SimpleMailMessage validMessage = messageFor(validEmail);
        List<String> mixedEmails = Arrays.asList(validEmail, null, "", "   ");

        stubSuccess(mixedEmails, Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));
        when(mailService.buildRideFinishedEmail(validEmail, RIDE_ID, START_ADDRESS, DEST_ADDRESS))
                .thenReturn(validMessage);

        service.finishRide(DRIVER_ID, RIDE_ID);

        InOrder inOrder = inOrder(
                repository, driverRepository, rideRepository, mailService, mailQueueService, notificationService
        );
        inOrder.verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        inOrder.verify(driverRepository).setAvailable(DRIVER_ID, true);
        inOrder.verify(rideRepository).findPassengerEmails(RIDE_ID);
        inOrder.verify(rideRepository).findRideAddresses(RIDE_ID);
        inOrder.verify(mailService).buildRideFinishedEmail(validEmail, RIDE_ID, START_ADDRESS, DEST_ADDRESS);
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        inOrder.verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        inOrder.verify(notificationService).notifyRideFinished(RIDE_ID);
        inOrder.verifyNoMoreInteractions();

        assertEquals(1, batchCaptor.getValue().size());
        assertEquals(validMessage, batchCaptor.getValue().get(0));
        verify(mailService, times(1)).buildRideFinishedEmail(validEmail, RIDE_ID, START_ADDRESS, DEST_ADDRESS);
    }

    @Test
    void finishRide_shouldUseFallbackAddresses_whenRideAddressesMissing() {
        String email = "fallback@test.com";
        SimpleMailMessage msg = messageFor(email);

        stubSuccess(List.of(email), Optional.empty());
        when(mailService.buildRideFinishedEmail(email, RIDE_ID, "", "")).thenReturn(msg);

        service.finishRide(DRIVER_ID, RIDE_ID);

        InOrder inOrder = inOrder(
                repository, driverRepository, rideRepository, mailService, mailQueueService, notificationService
        );
        inOrder.verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        inOrder.verify(driverRepository).setAvailable(DRIVER_ID, true);
        inOrder.verify(rideRepository).findPassengerEmails(RIDE_ID);
        inOrder.verify(rideRepository).findRideAddresses(RIDE_ID);
        inOrder.verify(mailService).buildRideFinishedEmail(email, RIDE_ID, "", "");
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        inOrder.verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        inOrder.verify(notificationService).notifyRideFinished(RIDE_ID);
        inOrder.verifyNoMoreInteractions();

        assertEquals(1, batchCaptor.getValue().size());
        assertEquals(msg, batchCaptor.getValue().get(0));
    }

    @Test
    void finishRide_shouldNotifyRideFinished_onSuccess() {
        stubSuccess(List.of(), Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));

        service.finishRide(DRIVER_ID, RIDE_ID);

        verify(notificationService).notifyRideFinished(RIDE_ID);
    }

    @Test
    void finishRide_shouldNotCallSideEffects_whenFinishFails() {
        when(repository.finishRide(DRIVER_ID, RIDE_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.finishRide(DRIVER_ID, RIDE_ID));

        verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        verifyNoInteractions(driverRepository, rideRepository, mailService, mailQueueService, notificationService);
    }

    @Test
    void finishRide_shouldCallSendBatchWithin_withTimeout20000() {
        stubSuccess(List.of(), Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));

        service.finishRide(DRIVER_ID, RIDE_ID);

        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        assertEquals(0, batchCaptor.getValue().size());
    }

    @Test
    void finishRide_shouldStillSendBatchCall_whenNoValidEmails() {
        List<String> invalidEmails = Arrays.asList(null, "", "   ");

        stubSuccess(invalidEmails, Optional.of(new RideRepository.RideAddresses(START_ADDRESS, DEST_ADDRESS)));

        service.finishRide(DRIVER_ID, RIDE_ID);

        InOrder inOrder = inOrder(
                repository, driverRepository, rideRepository, mailService, mailQueueService, notificationService
        );
        inOrder.verify(repository).finishRide(DRIVER_ID, RIDE_ID);
        inOrder.verify(driverRepository).setAvailable(DRIVER_ID, true);
        inOrder.verify(rideRepository).findPassengerEmails(RIDE_ID);
        inOrder.verify(rideRepository).findRideAddresses(RIDE_ID);
        ArgumentCaptor<List<SimpleMailMessage>> batchCaptor = batchCaptor();
        inOrder.verify(mailQueueService).sendBatchWithin(batchCaptor.capture(), eq(20_000L));
        inOrder.verify(notificationService).notifyRideFinished(RIDE_ID);
        inOrder.verifyNoMoreInteractions();

        assertEquals(0, batchCaptor.getValue().size());
        verifyNoInteractions(mailService);
    }

    private void stubSuccess(List<String> emails, Optional<RideRepository.RideAddresses> addresses) {
        when(repository.finishRide(DRIVER_ID, RIDE_ID)).thenReturn(true);
        when(rideRepository.findPassengerEmails(RIDE_ID)).thenReturn(emails);
        when(rideRepository.findRideAddresses(RIDE_ID)).thenReturn(addresses);
    }

    private static SimpleMailMessage messageFor(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        return message;
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<SimpleMailMessage>> batchCaptor() {
        return (ArgumentCaptor<List<SimpleMailMessage>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
    }
}
