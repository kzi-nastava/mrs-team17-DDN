package org.example.backend.service;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.repository.DriverRideRepository;
import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverRideService {

    private final DriverRideRepository repository;
    private final DriverRepository driverRepository;

    private final RideRepository rideRepository;
    private final MailService mailService;
    private final MailQueueService mailQueueService;
    private final NotificationService notificationService;

    public DriverRideService(
            DriverRideRepository repository,
            DriverRepository driverRepository,
            RideRepository rideRepository,
            MailService mailService,
            MailQueueService mailQueueService,
            NotificationService notificationService
    ) {
        this.repository = repository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.mailService = mailService;
        this.mailQueueService = mailQueueService;
        this.notificationService = notificationService;
    }

    public List<DriverRideHistoryResponseDto> getDriverRides(Long driverId, LocalDate from, LocalDate to) {
        return repository.findDriverRides(driverId, from, to);
    }

    public DriverRideDetailsResponseDto getDriverRideDetails(Long driverId, Long rideId) {
        return repository.findDriverRideDetails(driverId, rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }

    public DriverRideDetailsResponseDto getActiveRide(Long driverId) {
        return repository.findActiveRideDetails(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active ride"));
    }

    public List<DriverRideDetailsResponseDto> getAcceptedRides(Long driverId) {
        return repository.findAcceptedRides(driverId);
    }

    public List<DriverRideDetailsResponseDto> getUpcomingRides(Long driverId) {
        return repository.findUpcomingRides(driverId);
    }

    public void startRide(Long driverId, Long rideId) {
        DriverRideRepository.StartRideSnapshot snap = repository.findStartRideSnapshot(driverId, rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        if ("ACTIVE".equals(snap.status())) {
            driverRepository.setAvailable(driverId, false);
            return;
        }

        if (!"ACCEPTED".equals(snap.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride is not accepted");
        }

        if (snap.scheduledAt() != null) {
            OffsetDateTime now = OffsetDateTime.now();
            if (now.isBefore(snap.scheduledAt())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Scheduled ride cannot start before its time");
            }
        }

        if (!isNearPickup(snap)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is too far from pickup");
        }

        boolean ok = repository.startRide(driverId, rideId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found or cannot be started");
        }

        driverRepository.setAvailable(driverId, false);
    }

    public void finishRide(Long driverId, Long rideId) {
        boolean ok = repository.finishRide(driverId, rideId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found or cannot be finished");
        }

        boolean hasUpcomingAssignedRide = repository.hasUpcomingAssignedRide(driverId);
        driverRepository.setAvailable(driverId, !hasUpcomingAssignedRide);

        var emails = rideRepository.findPassengerEmails(rideId);
        var addresses = rideRepository.findRideAddresses(rideId)
                .orElse(new RideRepository.RideAddresses("", ""));

        List<SimpleMailMessage> out = new ArrayList<>();
        for (String email : emails) {
            if (email != null && !email.isBlank()) {
                out.add(
                        mailService.buildRideFinishedEmail(
                                email,
                                rideId,
                                addresses.startAddress(),
                                addresses.destinationAddress()
                        )
                );
            }
        }

        mailQueueService.sendBatchWithin(out, 20_000);
        notificationService.notifyRideFinished(rideId);
    }

    private static boolean isNearPickup(DriverRideRepository.StartRideSnapshot snap) {
        if (snap.pickupLat() == null || snap.pickupLng() == null || snap.carLat() == null || snap.carLng() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing coordinates for pickup check");
        }

        final double START_PICKUP_THRESHOLD_M = 80.0;
        double dist = haversineMeters(
                snap.carLat(), snap.carLng(),
                snap.pickupLat(), snap.pickupLng()
        );
        return dist <= START_PICKUP_THRESHOLD_M;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
