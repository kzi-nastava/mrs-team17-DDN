package org.example.backend.service;

import org.example.backend.repository.RideRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class RideSimulationScheduler {

    private final RideRepository repo;
    private final RideService rideService;

    public RideSimulationScheduler(RideRepository repo, RideService rideService) {
        this.repo = repo;
        this.rideService = rideService;
    }

    @Scheduled(fixedRate = 800)
    public void tick() {
        for (Long rideId : repo.findActiveRideIds()) {
            try {
                rideService.simulateVehicleStep(rideId);
            } catch (Exception ignored) {
            }
        }
    }
}
