package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.LatLngDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryRideRepository implements RideRepository {

    private final Map<Long, RideTrackingResponseDto> trackingByRideId = new HashMap<>();
    private final Map<Long, List<RideReportResponseDto>> reportsByRideId = new HashMap<>();
    private final Set<Long> finishedRideIds = new HashSet<>();

    private final AtomicLong reportIdSeq = new AtomicLong(1);

    public InMemoryRideRepository() {
        seed();
    }

    private void seed() {
        RideTrackingResponseDto t1 = new RideTrackingResponseDto();
        t1.setCar(new LatLngDto(45.2692, 19.8298));
        t1.setPickup(new LatLngDto(45.2671, 19.8335));
        t1.setDestination(new LatLngDto(45.2558, 19.8452));
        t1.setEtaMinutes(6);
        t1.setDistanceKm(2.4);
        t1.setStatus("ON_TRIP");
        trackingByRideId.put(1L, t1);

        RideTrackingResponseDto t2 = new RideTrackingResponseDto();
        t2.setCar(new LatLngDto(45.2600, 19.8350));
        t2.setPickup(new LatLngDto(45.2550, 19.8400));
        t2.setDestination(new LatLngDto(45.2480, 19.8500));
        t2.setEtaMinutes(9);
        t2.setDistanceKm(3.1);
        t2.setStatus("ON_TRIP");
        trackingByRideId.put(2L, t2);
    }

    @Override
    public Optional<RideTrackingResponseDto> findTrackingByRideId(Long rideId) {
        // ako je završena, možeš vratiti status FINISHED (opciono)
        RideTrackingResponseDto t = trackingByRideId.get(rideId);
        if (t == null) return Optional.empty();

        if (finishedRideIds.contains(rideId)) {
            RideTrackingResponseDto copy = new RideTrackingResponseDto();
            copy.setCar(t.getCar());
            copy.setPickup(t.getPickup());
            copy.setDestination(t.getDestination());
            copy.setEtaMinutes(0);
            copy.setDistanceKm(t.getDistanceKm());
            copy.setStatus("FINISHED");
            return Optional.of(copy);
        }

        return Optional.of(t);
    }

    @Override
    public RideReportResponseDto createReport(Long rideId, RideReportRequestDto request, OffsetDateTime now) {
        RideReportResponseDto response = new RideReportResponseDto();
        response.setId(reportIdSeq.getAndIncrement());
        response.setRideId(rideId);
        response.setDescription(request.getDescription());
        response.setCreatedAt(now);

        reportsByRideId.computeIfAbsent(rideId, k -> new ArrayList<>()).add(response);
        return response;
    }

    @Override
    public boolean finishRide(Long rideId) {
        // vraća false ako ride ne postoji u mock tracking map-i (da imamo 404)
        if (!trackingByRideId.containsKey(rideId)) return false;
        finishedRideIds.add(rideId);
        return true;
    }
}
