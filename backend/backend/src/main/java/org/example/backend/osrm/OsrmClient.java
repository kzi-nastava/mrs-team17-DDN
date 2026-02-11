package org.example.backend.osrm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class OsrmClient {

    private final HttpClient http;
    private final String baseUrl;
    private final ObjectMapper om = new ObjectMapper();

    public OsrmClient(@Value("${osrm.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /** Point(lat, lon) */
    public record Point(double lat, double lon) {}

    public record RouteSummary(double distanceMeters, double durationSeconds) {}

    public record RouteWithGeometry(
            double distanceMeters,
            double durationSeconds,
            List<Point> geometry // ordered polyline points (lat/lon)
    ) {}

    public Point nearestDrivingPoint(Point point) {
        return nearestDrivingPoint(point.lat(), point.lon());
    }

    public Point nearestDrivingPoint(double lat, double lon) {
        String url = baseUrl + "/nearest/v1/driving/" + lon + "," + lat + "?number=1";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());

            int sc = resp.statusCode();
            if (sc < 200 || sc >= 300) {
                throw new IllegalStateException("OSRM HTTP " + sc);
            }

            String json = new String(resp.body(), StandardCharsets.UTF_8);
            JsonNode root = om.readTree(json);
            JsonNode waypoints = root.path("waypoints");
            if (!waypoints.isArray() || waypoints.isEmpty()) {
                throw new IllegalStateException("OSRM JSON missing waypoints");
            }

            JsonNode loc = waypoints.get(0).path("location");
            if (!loc.isArray() || loc.size() < 2) {
                throw new IllegalStateException("OSRM waypoint location missing");
            }

            double snappedLon = loc.get(0).asDouble(Double.NaN);
            double snappedLat = loc.get(1).asDouble(Double.NaN);
            if (!Double.isFinite(snappedLat) || !Double.isFinite(snappedLon)) {
                throw new IllegalStateException("OSRM waypoint location invalid");
            }

            return new Point(snappedLat, snappedLon);

        } catch (Exception e) {
            String msg = (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            throw new IllegalStateException("OSRM nearest request failed: " + msg, e);
        }
    }

    public RouteSummary routeDriving(List<Point> points) {
        var full = routeDrivingWithGeometry(points, false);
        return new RouteSummary(full.distanceMeters(), full.durationSeconds());
    }

    public RouteWithGeometry routeDrivingWithGeometry(List<Point> points) {
        return routeDrivingWithGeometry(points, true);
    }

    private RouteWithGeometry routeDrivingWithGeometry(List<Point> points, boolean includeGeometry) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        String coords = points.stream()
                .map(p -> p.lon + "," + p.lat) // OSRM expects lon,lat
                .reduce((a, b) -> a + ";" + b)
                .orElseThrow();

        String url = baseUrl + "/route/v1/driving/" + coords
                + (includeGeometry
                ? "?overview=full&geometries=geojson"
                : "?overview=false");

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());

            int sc = resp.statusCode();
            if (sc < 200 || sc >= 300) {
                throw new IllegalStateException("OSRM HTTP " + sc);
            }

            String json = new String(resp.body(), StandardCharsets.UTF_8);

            JsonNode root = om.readTree(json);
            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                throw new IllegalStateException("OSRM JSON missing routes");
            }

            JsonNode r0 = routes.get(0);
            double distanceMeters = r0.path("distance").asDouble(Double.NaN);
            double durationSeconds = r0.path("duration").asDouble(Double.NaN);

            if (!Double.isFinite(distanceMeters) || !Double.isFinite(durationSeconds)) {
                throw new IllegalStateException("OSRM route duration/distance not found");
            }

            List<Point> geometry = List.of();
            if (includeGeometry) {
                geometry = new ArrayList<>();
                JsonNode coordsNode = r0.path("geometry").path("coordinates");
                if (!coordsNode.isArray() || coordsNode.isEmpty()) {
                    throw new IllegalStateException("OSRM route geometry missing");
                }
                for (JsonNode c : coordsNode) {
                    // [lon, lat]
                    double lon = c.get(0).asDouble();
                    double lat = c.get(1).asDouble();
                    geometry.add(new Point(lat, lon));
                }
            }

            return new RouteWithGeometry(distanceMeters, durationSeconds, geometry);

        } catch (Exception e) {
            String msg = (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            throw new IllegalStateException("OSRM request failed: " + msg, e);
        }
    }
}
