package org.example.backend.service;

import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.request.RidePointRequestDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.exception.NoAvailableDriverException;
import org.example.backend.osrm.OsrmClient;
import org.example.backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RideOrderService {

    private static final BigDecimal PRICE_PER_KM = new BigDecimal("120");

    private static final double FALLBACK_AVG_SPEED_KMH = 35.0;

    private final OsrmClient osrmClient;
    private final DriverMatchingRepository driverRepo;
    private final RideOrderRepository rideOrderRepo;
    private final RideStopRepository rideStopRepo;
    private final RidePassengerRepository passengerRepo;
    private final UserLookupRepository userLookupRepo;

    public RideOrderService(
            OsrmClient osrmClient,
            DriverMatchingRepository driverRepo,
            RideOrderRepository rideOrderRepo,
            RideStopRepository rideStopRepo,
            RidePassengerRepository passengerRepo,
            UserLookupRepository userLookupRepo
    ) {
        this.osrmClient = osrmClient;
        this.driverRepo = driverRepo;
        this.rideOrderRepo = rideOrderRepo;
        this.rideStopRepo = rideStopRepo;
        this.passengerRepo = passengerRepo;
        this.userLookupRepo = userLookupRepo;
    }

    @Transactional
    public CreateRideResponseDto createRide(CreateRideRequestDto req) {

        String orderType = safeLower(req.getOrderType());
        String vehicleType = safeLower(req.getVehicleType());

        if (!orderType.equals("now") && !orderType.equals("schedule")) {
            throw new IllegalArgumentException("orderType must be 'now' or 'schedule'");
        }

        if (!vehicleType.equals("standard") && !vehicleType.equals("luxury") && !vehicleType.equals("van")) {
            throw new IllegalArgumentException("vehicleType must be standard/luxury/van");
        }

        boolean baby = Boolean.TRUE.equals(req.getBabyTransport());
        boolean pet  = Boolean.TRUE.equals(req.getPetTransport());

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startedAt = null;

        if (orderType.equals("schedule")) {
            if (req.getScheduledAt() == null) {
                throw new IllegalArgumentException("scheduledAt is required for schedule order");
            }
            startedAt = req.getScheduledAt();

            if (startedAt.isBefore(now)) {
                throw new IllegalArgumentException("scheduledAt must be in the future");
            }
            if (startedAt.isAfter(now.plusHours(5))) {
                throw new IllegalArgumentException("scheduledAt can be at most 5 hours ahead");
            }
        }

        RidePointRequestDto start = req.getStart();
        RidePointRequestDto dest  = req.getDestination();

        List<DriverMatchingRepository.CandidateDriver> candidates =
                driverRepo.findAvailableDrivers(vehicleType, baby, pet);

        if (candidates.isEmpty()) {
            throw new NoAvailableDriverException("No available drivers for selected criteria");
        }

        DriverMatchingRepository.CandidateDriver chosen =
                chooseNearestToStart(candidates, start.getLat(), start.getLng());

        List<OsrmClient.Point> points = new ArrayList<>();
        points.add(new OsrmClient.Point(start.getLat(), start.getLng()));

        if (req.getCheckpoints() != null) {
            for (RidePointRequestDto cp : req.getCheckpoints()) {
                points.add(new OsrmClient.Point(cp.getLat(), cp.getLng()));
            }
        }

        points.add(new OsrmClient.Point(dest.getLat(), dest.getLng()));

        RouteMetrics metrics = calculateRouteMetrics(points);

        BigDecimal km = BigDecimal.valueOf(metrics.distanceMeters)
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal base = basePrice(vehicleType);
        BigDecimal price = base.add(km.multiply(PRICE_PER_KM))
                .setScale(2, RoundingMode.HALF_UP);

        UserLookupRepository.UserBasic requester = userLookupRepo.findById(req.getRequesterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Requester user not found"));

        if (!requester.active()) {
            throw new IllegalArgumentException("Requester account is not active");
        }

        List<RidePassengerRepository.PassengerRow> passengers = new ArrayList<>();
        passengers.add(new RidePassengerRepository.PassengerRow(
                requester.firstName() + " " + requester.lastName(),
                requester.email()
        ));

        int guestCounter = 1;

        if (req.getLinkedUsers() != null) {
            for (String raw : req.getLinkedUsers()) {
                String e = safeTrim(raw);

                if (e.isEmpty()) {
                    passengers.add(new RidePassengerRepository.PassengerRow("Guest " + guestCounter++, null));
                    continue;
                }

                var opt = userLookupRepo.findByEmail(e);

                if (opt.isEmpty()) {
                    passengers.add(new RidePassengerRepository.PassengerRow("Guest " + guestCounter++, null));
                    continue;
                }

                UserLookupRepository.UserBasic u = opt.get();
                passengers.add(new RidePassengerRepository.PassengerRow(
                        u.firstName() + " " + u.lastName(),
                        u.email()
                ));
            }
        }


        Long rideId = rideOrderRepo.insertRideReturningId(
                chosen.driverId(),
                startedAt,
                safeTrim(start.getAddress()),
                safeTrim(dest.getAddress()),
                price,
                "ACCEPTED",
                start.getLat(), start.getLng(),
                dest.getLat(), dest.getLng(),
                chosen.vehicleLat(), chosen.vehicleLng()
        );

        if (req.getCheckpoints() != null && !req.getCheckpoints().isEmpty()) {
            List<RideStopRepository.StopRow> stops = new ArrayList<>();
            int ord = 1;
            for (RidePointRequestDto cp : req.getCheckpoints()) {
                stops.add(new RideStopRepository.StopRow(
                        ord++,
                        cp.getAddress(),
                        cp.getLat(),
                        cp.getLng()
                ));
            }
            rideStopRepo.insertStops(rideId, stops);

        }

        passengerRepo.insertPassengers(rideId, passengers);

        driverRepo.setDriverAvailable(chosen.driverId(), false);

        CreateRideResponseDto resp = new CreateRideResponseDto();
        resp.setRideId(rideId);
        resp.setDriverId(chosen.driverId());
        resp.setStatus("ACCEPTED");
        resp.setPrice(price);
        return resp;
    }

    private static class RouteMetrics {
        final double distanceMeters;
        final double durationSeconds;
        final boolean osrmUsed;

        RouteMetrics(double distanceMeters, double durationSeconds, boolean osrmUsed) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.osrmUsed = osrmUsed;
        }
    }

    private RouteMetrics calculateRouteMetrics(List<OsrmClient.Point> points) {
        try {
            OsrmClient.RouteSummary summary = osrmClient.routeDriving(points);
            return new RouteMetrics(summary.distanceMeters(), summary.durationSeconds(), true);
        } catch (Exception ex) {
            double distanceMeters = fallbackDistanceMeters(points);
            double durationSeconds = fallbackDurationSeconds(distanceMeters);
            return new RouteMetrics(distanceMeters, durationSeconds, false);
        }
    }

    private static double fallbackDistanceMeters(List<OsrmClient.Point> pts) {
        if (pts == null || pts.size() < 2) return 0.0;

        double totalKm = 0.0;
        for (int i = 0; i < pts.size() - 1; i++) {
            OsrmClient.Point a = pts.get(i);
            OsrmClient.Point b = pts.get(i + 1);
            totalKm += haversineKm(a.lat(), a.lon(), b.lat(), b.lon());
        }
        return totalKm * 1000.0;
    }

    private static double fallbackDurationSeconds(double distanceMeters) {
        double km = distanceMeters / 1000.0;
        double hours = km / FALLBACK_AVG_SPEED_KMH;
        return hours * 3600.0;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeLower(String s) {
        return safeTrim(s).toLowerCase();
    }

    private static BigDecimal basePrice(String vehicleTypeLower) {
        if (vehicleTypeLower.equals("standard")) return new BigDecimal("200");
        if (vehicleTypeLower.equals("luxury"))   return new BigDecimal("300");
        if (vehicleTypeLower.equals("van"))      return new BigDecimal("250");
        return new BigDecimal("200");
    }

    private static DriverMatchingRepository.CandidateDriver chooseNearestToStart(
            List<DriverMatchingRepository.CandidateDriver> candidates,
            double startLat,
            double startLng
    ) {
        DriverMatchingRepository.CandidateDriver best = null;
        double bestKm = Double.POSITIVE_INFINITY;

        for (DriverMatchingRepository.CandidateDriver c : candidates) {
            double km = haversineKm(startLat, startLng, c.vehicleLat(), c.vehicleLng());
            if (km < bestKm) {
                bestKm = km;
                best = c;
            }
        }
        return best;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
