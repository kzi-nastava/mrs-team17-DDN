package com.example.taximobile.feature.user.data.dto.request;

import com.example.taximobile.feature.user.data.dto.response.LatLngDto;

import java.util.List;

public class RoutePreviewRequestDto {

    private List<LatLngDto> points;

    public RoutePreviewRequestDto() {}

    public RoutePreviewRequestDto(List<LatLngDto> points) {
        this.points = points;
    }

    public List<LatLngDto> getPoints() { return points; }
    public void setPoints(List<LatLngDto> points) { this.points = points; }
}
