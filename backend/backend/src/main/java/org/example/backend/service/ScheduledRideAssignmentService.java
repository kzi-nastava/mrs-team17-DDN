package org.example.backend.service;

import org.example.backend.repository.DriverMatchingRepository;
import org.example.backend.repository.ScheduledRideRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduledRideAssignmentService {

    private static final int ASSIGN_AHEAD_MINUTES = 10;

    private final ScheduledRideRepository scheduledRepo;
    private final DriverMatchingRepository driverMatching;

    public ScheduledRideAssignmentService(
            ScheduledRideRepository scheduledRepo,
            DriverMatchingRepository driverMatching
    ) {
        this.scheduledRepo = scheduledRepo;
        this.driverMatching = driverMatching;
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void assignDueScheduledRides() {
        List<ScheduledRideRepository.ScheduledRideRow> due =
                scheduledRepo.findDueScheduledRides(ASSIGN_AHEAD_MINUTES);

        for (ScheduledRideRepository.ScheduledRideRow r : due) {
            tryAssign(r);
        }
    }

    private void tryAssign(ScheduledRideRepository.ScheduledRideRow r) {

        var candidates = driverMatching.findAvailableDrivers(
                safeLower(r.vehicleType()),
                r.babyTransport(),
                r.petTransport(),
                r.requiredSeats()
        );

        if (candidates.isEmpty()) return;

        for (var c : candidates) {
            if (!driverMatching.tryClaimAvailableDriver(c.driverId())) continue;

            boolean assigned = scheduledRepo.assignDriverToScheduledRide(r.rideId(), c.driverId());
            if (!assigned) {
                driverMatching.setDriverAvailable(c.driverId(), true);
                return;
            }

            boolean accepted = scheduledRepo.markScheduledRideAccepted(r.rideId());
            if (!accepted) {
                driverMatching.setDriverAvailable(c.driverId(), true);
                return;
            }

            return;
        }
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
