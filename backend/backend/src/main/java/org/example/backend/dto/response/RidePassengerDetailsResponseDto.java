package org.example.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public class RidePassengerDetailsResponseDto {

    private Long rideId;
    private String status;

    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    private String startAddress;
    private String destinationAddress;

    private LatLngDto start;
    private LatLngDto destination;

    // ride_stops, with address + coordinates -> also used to prefill "repeat ride"
    private List<RideCheckpointDto> stops;

    // OSRM road-following polyline: start -> stops -> destination
    private List<LatLngDto> route;
    private double distanceKm;

    private List<RideReportDto> reports;

    // null if not rated (yet / at all)
    private RideRatingResponseDto rating;

    // null if no driver was ever assigned
    private DriverPublicInfoResponseDto driver;

    // needed to prefill "repeat ride"
    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    public RidePassengerDetailsResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public LatLngDto getStart() { return start; }
    public void setStart(LatLngDto start) { this.start = start; }

    public LatLngDto getDestination() { return destination; }
    public void setDestination(LatLngDto destination) { this.destination = destination; }

    public List<RideCheckpointDto> getStops() { return stops; }
    public void setStops(List<RideCheckpointDto> stops) { this.stops = stops; }

    public List<LatLngDto> getRoute() { return route; }
    public void setRoute(List<LatLngDto> route) { this.route = route; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public List<RideReportDto> getReports() { return reports; }
    public void setReports(List<RideReportDto> reports) { this.reports = reports; }

    public RideRatingResponseDto getRating() { return rating; }
    public void setRating(RideRatingResponseDto rating) { this.rating = rating; }

    public DriverPublicInfoResponseDto getDriver() { return driver; }
    public void setDriver(DriverPublicInfoResponseDto driver) { this.driver = driver; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isBabyTransport() { return babyTransport; }
    public void setBabyTransport(boolean babyTransport) { this.babyTransport = babyTransport; }

    public boolean isPetTransport() { return petTransport; }
    public void setPetTransport(boolean petTransport) { this.petTransport = petTransport; }
}
