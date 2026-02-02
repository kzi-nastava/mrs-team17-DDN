package org.example.backend.controller;

import org.example.backend.dto.request.RoutePreviewRequestDto;
import org.example.backend.dto.response.LatLngDto;
import org.example.backend.dto.response.RoutePreviewResponseDto;
import org.example.backend.osrm.OsrmClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

    private final OsrmClient osrm;

    public RoutingController(OsrmClient osrm) {
        this.osrm = osrm;
    }

    @PostMapping("/route")
    public RoutePreviewResponseDto previewRoute(@RequestBody RoutePreviewRequestDto req) {

        if (req == null || req.getPoints() == null || req.getPoints().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 2 points required");
        }

        List<OsrmClient.Point> pts = new ArrayList<>();
        for (LatLngDto p : req.getPoints()) {
            if (p == null) continue;
            pts.add(new OsrmClient.Point(p.getLat(), p.getLng()));
        }

        if (pts.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 2 valid points required");
        }

        try {
            // geometry + distance + duration za čitavu rutu kroz sve tačke
            var route = osrm.routeDrivingWithGeometry(pts);

            List<LatLngDto> geometry = route.geometry().stream()
                    .map(g -> new LatLngDto(g.lat(), g.lon()))
                    .toList();

            double distanceKm = route.distanceMeters() / 1000.0;
            int etaMinutes = (int) Math.max(1, Math.ceil(route.durationSeconds() / 60.0));

            distanceKm = Math.round(distanceKm * 100.0) / 100.0;

            return new RoutePreviewResponseDto(geometry, etaMinutes, distanceKm);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OSRM not available");
        }
    }
}
