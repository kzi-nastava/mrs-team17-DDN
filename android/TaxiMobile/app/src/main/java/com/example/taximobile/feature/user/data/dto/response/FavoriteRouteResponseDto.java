package com.example.taximobile.feature.user.data.dto.response;

import java.util.List;

public class FavoriteRouteResponseDto {
    private Long id;
    private String name;
    private FavoriteRoutePointResponseDto start;
    private FavoriteRoutePointResponseDto destination;
    private List<FavoriteRoutePointResponseDto> stops;

    public FavoriteRouteResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public FavoriteRoutePointResponseDto getStart() { return start; }
    public void setStart(FavoriteRoutePointResponseDto start) { this.start = start; }

    public FavoriteRoutePointResponseDto getDestination() { return destination; }
    public void setDestination(FavoriteRoutePointResponseDto destination) { this.destination = destination; }

    public List<FavoriteRoutePointResponseDto> getStops() { return stops; }
    public void setStops(List<FavoriteRoutePointResponseDto> stops) { this.stops = stops; }
}
