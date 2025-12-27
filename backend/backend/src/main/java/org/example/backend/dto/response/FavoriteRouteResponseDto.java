package org.example.backend.dto.response;

import java.util.List;

public class FavoriteRouteResponseDto {

    private Long id;
    private String startAddress;
    private String destinationAddress;
    private List<String> stops;

    public FavoriteRouteResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }
}
