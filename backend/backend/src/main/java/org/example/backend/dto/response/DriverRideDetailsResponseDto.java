// backend/src/main/java/org/example/backend/dto/response/DriverRideDetailsResponseDto.java
// (your file is OK; keep it as-is)
package org.example.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public class DriverRideDetailsResponseDto {

    private Long rideId;

    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    private String startAddress;
    private String destinationAddress;
    private List<String> stops;

    private boolean canceled;
    private String canceledBy;

    private double price;
    private boolean panicTriggered;

    private List<PassengerInfoResponseDto> passengers;

    public DriverRideDetailsResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getCanceledBy() { return canceledBy; }
    public void setCanceledBy(String canceledBy) { this.canceledBy = canceledBy; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isPanicTriggered() { return panicTriggered; }
    public void setPanicTriggered(boolean panicTriggered) { this.panicTriggered = panicTriggered; }

    public List<PassengerInfoResponseDto> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerInfoResponseDto> passengers) { this.passengers = passengers; }
}
