package org.example.backend.service;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.osrm.OsrmClient;
import org.example.backend.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RideService {

    private final RideRepository repository;
    private final OsrmClient osrm;
    private final MailService mailService;

    public RideService(RideRepository repository, OsrmClient osrm, MailService mailService) {
        this.repository = repository;
        this.osrm = osrm;
        this.mailService = mailService;
    }

    // NEW: passenger "my active ride"
    public Long getActiveRideIdForPassenger(long userId) {
        return repository.findActiveRideIdForPassenger(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active ride"));
    }


    public RideTrackingResponseDto getRideTracking(Long rideId) {
        return repository.findTrackingByRideId(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride tracking not found"));
    }

    public RideReportResponseDto reportRideIssue(Long rideId, RideReportRequestDto request) {
        try {
            return repository.createReport(rideId, request, OffsetDateTime.now());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Long getRideIdToRateForPassenger(long userId) {
        return repository.findRideIdToRateForPassenger(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No ride to rate"));
    }

    public void finishRide(Long rideId) {
        boolean ok = repository.finishRide(rideId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        var emails = repository.findPassengerEmails(rideId);

        var addresses = repository.findRideAddresses(rideId)
                .orElse(new RideRepository.RideAddresses("", ""));

        for (String email : emails) {
            mailService.sendRideFinishedEmail(
                    email,
                    rideId,
                    addresses.startAddress(),
                    addresses.destinationAddress()
            );
        }
    }

    /**
     * Simulation tick:
     * - While pickedUp=false -> move towards pickup
     * - Once within PICKUP_THRESHOLD_M -> mark picked_up=true (one-way switch)
     * - When pickedUp=true -> move towards destination
     *
     * IMPORTANT: Never auto-finish. Completing ride is driver's job (finish endpoint).
     *
     * Requires:
     * - rides.picked_up boolean not null default false
     * - snapshot includes pickedUp
     * - repository.markPickedUp(rideId)
     */
    public void simulateVehicleStep(Long rideId) {
        var snap = repository.findMoveSnapshot(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride/vehicle not found"));

        if (snap.endedAt() != null || snap.canceled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride already ended/canceled");
        }

        if (!"ACTIVE".equals(snap.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is not ACTIVE");
        }

        final double PICKUP_THRESHOLD_M = 80.0;
        final double STOP_THRESHOLD_M = 60.0;
        final double DEST_THRESHOLD_M = 30.0;
        final double STEP_METERS = 25.0;

        boolean pickedUp = snap.pickedUp();

        if (!pickedUp) {
            double distToPickup = haversineMeters(
                    snap.carLat(), snap.carLng(),
                    snap.pickupLat(), snap.pickupLng()
            );

            if (distToPickup <= PICKUP_THRESHOLD_M) {
                repository.markPickedUp(rideId);
                repository.updateVehicleLocation(snap.driverId(), snap.pickupLat(), snap.pickupLng());
                return;
            }
        }

        double targetLat;
        double targetLng;

        if (!pickedUp) {
            targetLat = snap.pickupLat();
            targetLng = snap.pickupLng();
        } else {
            var stops = repository.findRideStopPoints(rideId);
            int idx = Math.max(0, snap.nextStopIndex());

            if (idx < stops.size()) {
                var nextStop = stops.get(idx);

                double distToStop = haversineMeters(
                        snap.carLat(), snap.carLng(),
                        nextStop.lat(), nextStop.lng()
                );

                if (distToStop <= STOP_THRESHOLD_M) {
                    repository.setNextStopIndex(rideId, idx + 1);
                    repository.updateVehicleLocation(snap.driverId(), nextStop.lat(), nextStop.lng());
                    return;
                }

                targetLat = nextStop.lat();
                targetLng = nextStop.lng();

            } else {
                double distToDest = haversineMeters(
                        snap.carLat(), snap.carLng(),
                        snap.destLat(), snap.destLng()
                );

                if (distToDest <= DEST_THRESHOLD_M) {
                    repository.updateVehicleLocation(snap.driverId(), snap.destLat(), snap.destLng());
                    return;
                }

                targetLat = snap.destLat();
                targetLng = snap.destLng();
            }
        }

        OsrmClient.RouteWithGeometry route;
        try {
            route = osrm.routeDrivingWithGeometry(
                    List.of(
                            new OsrmClient.Point(snap.carLat(), snap.carLng()),
                            new OsrmClient.Point(targetLat, targetLng)
                    )
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OSRM route geometry not available");
        }

        if (route.geometry() == null || route.geometry().size() < 2) return;

        var next = advanceAlongPolylineMeters(
                snap.carLat(), snap.carLng(),
                route.geometry(),
                STEP_METERS
        );

        boolean updated = repository.updateVehicleLocation(snap.driverId(), next.lat(), next.lng());
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle location not updated");
        }
    }

    private record LatLng(double lat, double lng) {}

    private static LatLng advanceAlongPolylineMeters(
            double startLat,
            double startLng,
            List<OsrmClient.Point> geometry,
            double stepMeters
    ) {
        if (geometry == null || geometry.size() < 2) {
            return new LatLng(startLat, startLng);
        }

        double remaining = stepMeters;

        double curLat = startLat;
        double curLng = startLng;

        for (int i = 0; i < geometry.size() - 1; i++) {
            OsrmClient.Point a = (i == 0) ? new OsrmClient.Point(curLat, curLng) : geometry.get(i);
            OsrmClient.Point b = geometry.get(i + 1);

            double segLen = haversineMeters(a.lat(), a.lon(), b.lat(), b.lon());
            if (segLen <= 0.01) continue;

            if (remaining < segLen) {
                double t = remaining / segLen;
                return lerpOnSphereApprox(a.lat(), a.lon(), b.lat(), b.lon(), t);
            }

            remaining -= segLen;
            curLat = b.lat();
            curLng = b.lon();
        }

        OsrmClient.Point last = geometry.get(geometry.size() - 1);
        return new LatLng(last.lat(), last.lon());
    }

    private static LatLng lerpOnSphereApprox(double lat1, double lon1, double lat2, double lon2, double t) {
        double lat = lat1 + (lat2 - lat1) * t;
        double lon = lon1 + (lon2 - lon1) * t;
        return new LatLng(lat, lon);
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

    public void startRide(Long rideId) {
        boolean ok = repository.startRide(rideId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found or cannot be started");
        }
    }
}
