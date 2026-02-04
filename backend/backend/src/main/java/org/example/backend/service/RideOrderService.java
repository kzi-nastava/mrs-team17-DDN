package org.example.backend.service;

import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.request.RidePointRequestDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.exception.ActiveRideConflictException;
import org.example.backend.exception.NoAvailableDriverException;
import org.example.backend.osrm.OsrmClient;
import org.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RideOrderService {

    private static final BigDecimal PRICE_PER_KM = new BigDecimal("120");
    private static final double FALLBACK_AVG_SPEED_KMH = 35.0;
    private static final int FINISHING_SOON_THRESHOLD_SECONDS = 10 * 60;

    private final OsrmClient osrmClient;
    private final DriverMatchingRepository driverRepo;
    private final RideOrderRepository rideOrderRepo;
    private final RideStopRepository rideStopRepo;
    private final RidePassengerRepository passengerRepo;
    private final UserLookupRepository userLookupRepo;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final MailQueueService mailQueueService;
    private final PricingService pricingService;


    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public RideOrderService(
            OsrmClient osrmClient,
            DriverMatchingRepository driverRepo,
            RideOrderRepository rideOrderRepo,
            RideStopRepository rideStopRepo,
            RidePassengerRepository passengerRepo,
            UserLookupRepository userLookupRepo,
            MailService mailService,
            NotificationService notificationService,
            MailQueueService mailQueueService, PricingService pricingService
    ) {
        this.osrmClient = osrmClient;
        this.driverRepo = driverRepo;
        this.rideOrderRepo = rideOrderRepo;
        this.rideStopRepo = rideStopRepo;
        this.passengerRepo = passengerRepo;
        this.userLookupRepo = userLookupRepo;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.mailQueueService = mailQueueService;
        this.pricingService = pricingService;
    }

    @Transactional
    public CreateRideResponseDto createRide(long requesterUserId, CreateRideRequestDto req) {

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
        OffsetDateTime scheduledAt = null;

        if (orderType.equals("schedule")) {
            if (req.getScheduledAt() == null) {
                throw new IllegalArgumentException("scheduledAt is required for schedule order");
            }
            scheduledAt = req.getScheduledAt();

            if (scheduledAt.isBefore(now)) {
                throw new IllegalArgumentException("scheduledAt must be in the future");
            }
            if (scheduledAt.isAfter(now.plusHours(5))) {
                throw new IllegalArgumentException("scheduledAt can be at most 5 hours ahead");
            }
        }

        RidePointRequestDto start = req.getStart();
        RidePointRequestDto dest  = req.getDestination();

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

        BigDecimal base = pricingService.basePrice(vehicleType);
        BigDecimal price = base.add(km.multiply(PRICE_PER_KM))
                .setScale(2, RoundingMode.HALF_UP);

        UserLookupRepository.UserBasic requester = userLookupRepo.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("Requester user not found"));

        if (!requester.active()) {
            throw new IllegalArgumentException("Requester account is not active");
        }
        if (requester.blocked()) {
            String reason = requester.blockReason() == null ? "" : (" Reason: " + requester.blockReason());
            throw new IllegalArgumentException("Requester is blocked." + reason);
        }

        if (rideOrderRepo.userHasActiveRide(requester.email())) {
            throw new ActiveRideConflictException("User already has an active ride.");
        }

        List<RidePassengerRepository.PassengerRow> passengers = new ArrayList<>();
        passengers.add(new RidePassengerRepository.PassengerRow(
                requester.firstName() + " " + requester.lastName(),
                requester.email()
        ));

        Set<String> addedEmails = new HashSet<>();
        if (requester.email() != null) {
            addedEmails.add(requester.email().trim().toLowerCase());
        }

        int requiredSeats = 1;

        if (req.getLinkedUsers() != null) {
            for (String raw : req.getLinkedUsers()) {
                String e = safeTrim(raw);

                if (e.isEmpty()) {
                    requiredSeats++;
                    continue;
                }

                String lower = e.toLowerCase();
                if (addedEmails.contains(lower)) continue;

                var opt = userLookupRepo.findByEmail(e);

                if (opt.isEmpty()) {
                    requiredSeats++;
                    continue;
                }

                UserLookupRepository.UserBasic u = opt.get();

                if (!u.active()) {
                    throw new IllegalArgumentException("Linked user is not active: " + e);
                }
                if (u.blocked()) {
                    throw new IllegalArgumentException("Linked user is blocked: " + e);
                }

                if (rideOrderRepo.userHasActiveRide(u.email())) {
                    throw new ActiveRideConflictException("One of the linked users already has an active ride.");
                }

                passengers.add(new RidePassengerRepository.PassengerRow(
                        u.firstName() + " " + u.lastName(),
                        u.email()
                ));
                addedEmails.add(lower);

                requiredSeats++;
            }
        }

        if (orderType.equals("schedule")) {

            Long rideId = rideOrderRepo.insertScheduledRideReturningId(
                    scheduledAt,
                    safeTrim(start.getAddress()),
                    safeTrim(dest.getAddress()),
                    price,
                    "SCHEDULED",
                    start.getLat(), start.getLng(),
                    dest.getLat(), dest.getLng(),
                    metrics.distanceMeters,
                    metrics.durationSeconds,
                    vehicleType,
                    baby,
                    pet,
                    requiredSeats
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

            CreateRideResponseDto resp = new CreateRideResponseDto();
            resp.setRideId(rideId);
            resp.setDriverId(null);
            resp.setStatus("SCHEDULED");
            resp.setPrice(price);
            return resp;
        }

        DriverPick pick = null;
        Long rideId = null;

        pick = reserveNearestAvailableDriver(vehicleType, baby, pet, requiredSeats, start.getLat(), start.getLng());
        if (pick != null) {
            try {
                rideId = insertRide(pick, scheduledAt, start, dest, price, metrics);
            } catch (DataIntegrityViolationException ex) {
                driverRepo.setDriverAvailable(pick.driverId, true);

                if (isUniqueViolation(ex)) {
                    pick = null;
                } else {
                    throw ex;
                }
            }
        }

        if (rideId == null) {
            List<DriverPick> finishingSoonPicks =
                    finishingSoonPicksOrdered(vehicleType, baby, pet, requiredSeats, start.getLat(), start.getLng());

            for (DriverPick candidate : finishingSoonPicks) {
                try {
                    rideId = insertRide(candidate, scheduledAt, start, dest, price, metrics);
                    pick = candidate;

                    driverRepo.setDriverAvailable(pick.driverId, false);
                    break;
                } catch (DataIntegrityViolationException ex) {
                    if (isUniqueViolation(ex)) {
                        continue;
                    }
                    throw ex;
                }
            }
        }

        if (rideId == null || pick == null) {
            throw new NoAvailableDriverException("No available drivers for selected criteria");
        }

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

        String startAddr = safeTrim(start.getAddress());
        String destAddr  = safeTrim(dest.getAddress());
        String trackingLink = frontendBaseUrl + "/user/ride-tracking?rideId=" + rideId;

        List<SimpleMailMessage> out = new ArrayList<>();
        for (RidePassengerRepository.PassengerRow p : passengers) {
            String email = p.email() == null ? "" : p.email().trim();
            if (!email.isEmpty()) {
                out.add(
                        mailService.buildRideAcceptedEmail(
                                email,
                                rideId,
                                startAddr,
                                destAddr,
                                trackingLink
                        )
                );
            }
        }
        mailQueueService.sendBatchWithin(out, 10_000);

        notificationService.notifyRideAccepted(rideId);

        CreateRideResponseDto resp = new CreateRideResponseDto();
        resp.setRideId(rideId);
        resp.setDriverId(pick.driverId);
        resp.setStatus("ACCEPTED");
        resp.setPrice(price);
        return resp;
    }

    private Long insertRide(DriverPick pick,
                            OffsetDateTime scheduledAt,
                            RidePointRequestDto start,
                            RidePointRequestDto dest,
                            BigDecimal price,
                            RouteMetrics metrics) {

        return rideOrderRepo.insertRideReturningId(
                pick.driverId,
                scheduledAt,
                safeTrim(start.getAddress()),
                safeTrim(dest.getAddress()),
                price,
                "ACCEPTED",
                start.getLat(), start.getLng(),
                dest.getLat(), dest.getLng(),
                pick.carLat, pick.carLng,
                metrics.distanceMeters,
                metrics.durationSeconds
        );
    }

    private static class DriverPick {
        final Long driverId;
        final double carLat;
        final double carLng;

        DriverPick(Long driverId, double carLat, double carLng) {
            this.driverId = driverId;
            this.carLat = carLat;
            this.carLng = carLng;
        }
    }

    private DriverPick reserveNearestAvailableDriver(String vehicleType,
                                                     boolean baby,
                                                     boolean pet,
                                                     int requiredSeats,
                                                     double startLat,
                                                     double startLng) {

        for (int attempt = 0; attempt < 2; attempt++) {
            List<DriverMatchingRepository.CandidateDriver> candidates =
                    driverRepo.findAvailableDrivers(vehicleType, baby, pet, requiredSeats);

            if (candidates.isEmpty()) return null;

            List<CandidateWithDist> ordered = new ArrayList<>();
            for (DriverMatchingRepository.CandidateDriver c : candidates) {
                double km = haversineKm(startLat, startLng, c.vehicleLat(), c.vehicleLng());
                ordered.add(new CandidateWithDist(c, km));
            }
            ordered.sort(Comparator.comparingDouble(o -> o.distKm));

            for (CandidateWithDist cand : ordered) {
                Long driverId = cand.c.driverId();
                if (driverRepo.tryClaimAvailableDriver(driverId)) {
                    return new DriverPick(driverId, cand.c.vehicleLat(), cand.c.vehicleLng());
                }
            }
        }

        return null;
    }

    private static class CandidateWithDist {
        final DriverMatchingRepository.CandidateDriver c;
        final double distKm;

        CandidateWithDist(DriverMatchingRepository.CandidateDriver c, double distKm) {
            this.c = c;
            this.distKm = distKm;
        }
    }

    private List<DriverPick> finishingSoonPicksOrdered(String vehicleType,
                                                       boolean baby,
                                                       boolean pet,
                                                       int requiredSeats,
                                                       double startLat,
                                                       double startLng) {

        List<DriverMatchingRepository.FinishingSoonDriver> fs =
                driverRepo.findDriversFinishingSoon(vehicleType, baby, pet, requiredSeats, FINISHING_SOON_THRESHOLD_SECONDS);

        if (fs.isEmpty()) return Collections.emptyList();

        List<FinishingWithDist> ordered = new ArrayList<>();
        for (DriverMatchingRepository.FinishingSoonDriver d : fs) {
            double kmToFinish = haversineKm(startLat, startLng, d.finishLat(), d.finishLng());
            ordered.add(new FinishingWithDist(d, kmToFinish));
        }
        ordered.sort(Comparator.comparingDouble(o -> o.distKmToFinish));

        List<DriverPick> out = new ArrayList<>();
        for (FinishingWithDist o : ordered) {
            out.add(new DriverPick(o.d.driverId(), o.d.vehicleLat(), o.d.vehicleLng()));
        }
        return out;
    }

    private static class FinishingWithDist {
        final DriverMatchingRepository.FinishingSoonDriver d;
        final double distKmToFinish;

        FinishingWithDist(DriverMatchingRepository.FinishingSoonDriver d, double distKmToFinish) {
            this.d = d;
            this.distKmToFinish = distKmToFinish;
        }
    }

    private static boolean isUniqueViolation(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException) {
                String state = ((SQLException) t).getSQLState();
                if ("23505".equals(state)) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
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
