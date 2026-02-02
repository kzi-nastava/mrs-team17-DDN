package org.example.backend.dto.request;

import org.example.backend.dto.response.LatLngDto;

import java.util.List;

public class RoutePreviewRequestDto {

    private List<LatLngDto> points;

    public RoutePreviewRequestDto() {}

    public List<LatLngDto> getPoints() {
        return points;
    }

    public void setPoints(List<LatLngDto> points) {
        this.points = points;
    }
}
