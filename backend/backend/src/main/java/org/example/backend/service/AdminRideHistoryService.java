package org.example.backend.service;

import org.example.backend.dto.request.AdminReorderRideRequestDto;
import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.request.RidePointRequestDto;
import org.example.backend.dto.response.AdminRideDetailsResponseDto;
import org.example.backend.dto.response.AdminRideHistoryItemDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.repository.AdminRideHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminRideHistoryService {

    private final AdminRideHistoryRepository repo;
    private final RideOrderService rideOrderService;

    public AdminRideHistoryService(AdminRideHistoryRepository repo, RideOrderService rideOrderService) {
        this.repo = repo;
        this.rideOrderService = rideOrderService;
    }

    public List<AdminRideHistoryItemDto> getAllRides(LocalDate from, LocalDate to, String sort, String dir) {
        return repo.listAll(from, to, sort, dir);
    }

    public List<AdminRideHistoryItemDto> getDriverRides(long driverId, LocalDate from, LocalDate to, String sort, String dir) {
        return repo.listByDriver(driverId, from, to, sort, dir);
    }

    public List<AdminRideHistoryItemDto> getPassengerRides(long userId, LocalDate from, LocalDate to, String sort, String dir) {
        return repo.listByPassenger(userId, from, to, sort, dir);
    }

    public AdminRideDetailsResponseDto getRideDetails(long rideId) {
        return repo.findDetails(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }

    @Transactional
    public CreateRideResponseDto reorderSameRoute(long rideId, AdminReorderRideRequestDto request) {
        AdminRideHistoryRepository.ReorderSource source = repo.findReorderSource(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        if (source.passengerUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This ride has no identifiable passenger to order for"
            );
        }

        OffsetDateTime scheduledAt = request != null ? request.getScheduledAt() : null;

        CreateRideRequestDto req = new CreateRideRequestDto();
        req.setOrderType(scheduledAt != null ? "schedule" : "now");
        req.setScheduledAt(scheduledAt);

        RidePointRequestDto start = new RidePointRequestDto();
        start.setAddress(source.startAddress());
        start.setLat(source.startLat());
        start.setLng(source.startLng());
        req.setStart(start);

        RidePointRequestDto destination = new RidePointRequestDto();
        destination.setAddress(source.destinationAddress());
        destination.setLat(source.destLat());
        destination.setLng(source.destLng());
        req.setDestination(destination);

        List<RidePointRequestDto> checkpoints = new ArrayList<>();
        if (source.stops() != null) {
            for (AdminRideHistoryRepository.ReorderStop stop : source.stops()) {
                RidePointRequestDto cp = new RidePointRequestDto();
                cp.setAddress(stop.address());
                cp.setLat(stop.lat());
                cp.setLng(stop.lng());
                checkpoints.add(cp);
            }
        }
        req.setCheckpoints(checkpoints);

        req.setVehicleType(source.vehicleType());
        req.setBabyTransport(source.babyTransport());
        req.setPetTransport(source.petTransport());

        return rideOrderService.createRide(source.passengerUserId(), req);
    }
}
