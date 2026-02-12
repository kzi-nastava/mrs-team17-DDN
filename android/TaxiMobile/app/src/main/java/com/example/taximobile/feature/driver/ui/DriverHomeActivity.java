package com.example.taximobile.feature.driver.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taximobile.R;
import com.example.taximobile.feature.driver.data.DriverRideRepository;
import com.example.taximobile.feature.driver.data.DriverRideTrackingRepository;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;
import com.example.taximobile.feature.user.data.dto.response.LatLngDto;
import com.example.taximobile.feature.user.data.dto.response.RideCheckpointDto;
import com.example.taximobile.feature.user.data.dto.response.RideTrackingResponseDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverHomeActivity extends DriverBaseActivity {

    private DriverRideRepository rideRepo;
    private DriverRideTrackingRepository trackingRepo;

    private ProgressBar progress;
    private TextView tvTitle;
    private TextView tvEmptyState;
    private TextView tvStart;
    private TextView tvEnd;
    private TextView tvMeta;
    private TextView tvCheckpointsLabel;
    private TextView tvCheckpointsList;
    private TextView tvError;
    private Button btnStart;
    private Button btnFutureRides;

    private MapView map;

    private DriverRideDetailsResponseDto assignedRide;
    private RideTrackingResponseDto tracking;

    private Marker pickupMarker;
    private Marker destMarker;
    private Marker carMarker;
    private final List<Marker> checkpointMarkers = new ArrayList<>();
    private Polyline routeLine;

    private Drawable pickupIcon;
    private Drawable destIcon;
    private Drawable carIcon;
    private Drawable checkpointIcon;

    private boolean firstCamera = true;
    private boolean loading = false;
    private boolean starting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_driver_home_start_ride);
        toolbar.setTitle(getString(R.string.menu_home));

        progress = contentView.findViewById(R.id.dhProgress);
        tvTitle = contentView.findViewById(R.id.dhTitle);
        tvEmptyState = contentView.findViewById(R.id.dhEmptyState);
        tvStart = contentView.findViewById(R.id.dhRouteStart);
        tvEnd = contentView.findViewById(R.id.dhRouteEnd);
        tvMeta = contentView.findViewById(R.id.dhMeta);
        tvCheckpointsLabel = contentView.findViewById(R.id.dhCheckpointsLabel);
        tvCheckpointsList = contentView.findViewById(R.id.dhCheckpointsList);
        tvError = contentView.findViewById(R.id.dhError);
        btnStart = contentView.findViewById(R.id.dhStartBtn);
        btnFutureRides = contentView.findViewById(R.id.dhFutureRidesBtn);
        map = contentView.findViewById(R.id.dhMap);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.5);
        map.getController().setCenter(new GeoPoint(45.2671, 19.8335)); // Novi Sad default

        pickupIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_pickup);
        destIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_destination);
        carIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_car);
        checkpointIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_checkpoint);

        rideRepo = new DriverRideRepository(this);
        trackingRepo = new DriverRideTrackingRepository(this);

        btnStart.setOnClickListener(v -> onStartClicked());
        btnFutureRides.setOnClickListener(v ->
                startActivity(new Intent(this, DriverFutureRidesActivity.class)));

        setLoading(true);
        bootstrap();
    }

    private void bootstrap() {
        rideRepo.getActiveRide(new DriverRideRepository.ActiveRideCb() {
            @Override
            public void onSuccess(DriverRideDetailsResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);
                    openActiveRide();
                });
            }

            @Override
            public void onEmpty() {
                runOnUiThread(DriverHomeActivity.this::loadAcceptedRide);
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    showError(getString(R.string.driver_start_ride_error_check_active, msg));
                    loadAcceptedRide();
                });
            }
        });
    }

    private void loadAcceptedRide() {
        setLoading(true);
        clearRouteFromMap();

        rideRepo.getAcceptedRides(new DriverRideRepository.AcceptedRidesCb() {
            @Override
            public void onSuccess(List<DriverRideDetailsResponseDto> rides) {
                runOnUiThread(() -> {
                    setLoading(false);

                    if (rides == null || rides.isEmpty()) {
                        assignedRide = null;
                        tracking = null;
                        renderNoRide();
                        return;
                    }

                    assignedRide = rides.get(0);
                    tracking = null;
                    renderRide(assignedRide);

                    Long rideId = assignedRide.getRideId();
                    if (rideId != null) {
                        loadTrackingOnce(rideId.longValue());
                    }
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    assignedRide = null;
                    tracking = null;
                    renderNoRide();
                    showError(getString(R.string.driver_start_ride_error_load_assigned, msg));
                });
            }
        });
    }

    private void loadTrackingOnce(long rideId) {
        trackingRepo.getTracking(rideId, new DriverRideTrackingRepository.TrackingCb() {
            @Override
            public void onSuccess(RideTrackingResponseDto dto) {
                runOnUiThread(() -> {
                    tracking = dto;
                    renderMeta();
                    renderCheckpoints();
                    if (tracking != null) drawRouteOnMap(tracking);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    tracking = null;
                    renderMeta();
                    renderCheckpoints();
                });
            }
        });
    }

    private void renderNoRide() {
        tvTitle.setText(getString(R.string.menu_home));

        tvEmptyState.setVisibility(View.VISIBLE);
        btnFutureRides.setVisibility(View.VISIBLE);

        tvStart.setVisibility(View.GONE);
        tvEnd.setVisibility(View.GONE);
        tvMeta.setVisibility(View.GONE);
        tvCheckpointsLabel.setVisibility(View.GONE);
        tvCheckpointsList.setVisibility(View.GONE);

        btnStart.setEnabled(false);
        btnStart.setText(getString(R.string.driver_start_ride_start_button));

        map.setVisibility(View.GONE);

        showError(null);
    }

    private void renderRide(DriverRideDetailsResponseDto ride) {
        map.setVisibility(View.VISIBLE);

        tvEmptyState.setVisibility(View.GONE);
        btnFutureRides.setVisibility(View.GONE);
        tvStart.setVisibility(View.VISIBLE);
        tvEnd.setVisibility(View.VISIBLE);
        tvMeta.setVisibility(View.VISIBLE);

        Long rideId = ride != null ? ride.getRideId() : null;
        tvTitle.setText(rideId != null ? ("Ride #" + rideId) : getString(R.string.driver_start_ride_title_default));

        String startAddr = ride != null && ride.getStartAddress() != null ? ride.getStartAddress() : "-";
        String destAddr = ride != null && ride.getDestinationAddress() != null ? ride.getDestinationAddress() : "-";

        tvStart.setText(getString(R.string.driver_start_ride_route_start, startAddr));
        tvEnd.setText(getString(R.string.driver_start_ride_route_end, destAddr));

        renderMeta();
        renderCheckpoints();
        showError(null);

        boolean canStart = ride != null && !loading && !starting;
        btnStart.setEnabled(canStart);
        btnStart.setText(getString(R.string.driver_start_ride_start_button));
    }

    private void renderMeta() {
        if (assignedRide == null) {
            tvMeta.setText(getString(R.string.driver_start_ride_meta_default));
            return;
        }

        String status = assignedRide.getStatus() != null ? assignedRide.getStatus() : "-";
        double price = assignedRide.getPrice();

        if (tracking != null) {
            tvMeta.setText(String.format(
                    Locale.US,
                    "Status: %s • ETA: %d min (%.2f km) • Price: %.2f",
                    status,
                    tracking.getEtaMinutes(),
                    tracking.getDistanceKm(),
                    price
            ));
        } else {
            tvMeta.setText(String.format(
                    Locale.US,
                    "Status: %s • Price: %.2f",
                    status,
                    price
            ));
        }
    }

    private void renderCheckpoints() {
        if (tracking == null || tracking.getCheckpoints() == null || tracking.getCheckpoints().isEmpty()) {
            tvCheckpointsLabel.setVisibility(View.GONE);
            tvCheckpointsList.setVisibility(View.GONE);
            tvCheckpointsList.setText("");
            return;
        }

        List<RideCheckpointDto> cps = tracking.getCheckpoints();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cps.size(); i++) {
            RideCheckpointDto c = cps.get(i);
            if (c == null) continue;

            String addr = c.getAddress();
            if (addr == null || addr.trim().isEmpty()) {
                addr = String.format(Locale.US, "%.6f, %.6f", c.getLat(), c.getLng());
            }

            sb.append("• ").append(addr.trim());
            if (i != cps.size() - 1) sb.append("\n");
        }

        if (sb.length() == 0) {
            tvCheckpointsLabel.setVisibility(View.GONE);
            tvCheckpointsList.setVisibility(View.GONE);
            tvCheckpointsList.setText("");
            return;
        }

        tvCheckpointsLabel.setVisibility(View.VISIBLE);
        tvCheckpointsList.setVisibility(View.VISIBLE);
        tvCheckpointsList.setText(sb.toString());
    }

    private void onStartClicked() {
        if (assignedRide == null || assignedRide.getRideId() == null) return;
        if (starting || loading) return;

        if ("ACTIVE".equalsIgnoreCase(assignedRide.getStatus())) {
            openActiveRide();
            return;
        }

        starting = true;
        btnStart.setEnabled(false);
        btnStart.setText(getString(R.string.driver_start_ride_starting_button));

        long rideId = assignedRide.getRideId();

        rideRepo.startRide(rideId, new DriverRideRepository.StartCb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    starting = false;
                    btnStart.setText(getString(R.string.driver_start_ride_start_button));
                    openActiveRide();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    starting = false;
                    btnStart.setText(getString(R.string.driver_start_ride_start_button));
                    btnStart.setEnabled(true);
                    showError(getString(R.string.driver_start_ride_error_start, msg));
                });
            }
        });
    }

    private void openActiveRide() {
        startActivity(new Intent(this, DriverActiveRideActivity.class));
    }

    private void setLoading(boolean value) {
        loading = value;
        progress.setVisibility(value ? View.VISIBLE : View.GONE);

        if (btnStart != null) {
            btnStart.setEnabled(!value && !starting && assignedRide != null);
        }
    }

    private void showError(String msg) {
        if (tvError == null) return;

        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void drawRouteOnMap(RideTrackingResponseDto t) {
        if (map == null || t == null) return;

        clearRouteFromMap();

        List<LatLngDto> route = t.getRoute();
        List<GeoPoint> pts = new ArrayList<>();
        if (route != null && !route.isEmpty()) {
            for (LatLngDto p : route) {
                if (p == null) continue;
                pts.add(new GeoPoint(p.getLat(), p.getLng()));
            }
        } else if (t.getPickup() != null && t.getDestination() != null) {
            pts.add(new GeoPoint(t.getPickup().getLat(), t.getPickup().getLng()));
            pts.add(new GeoPoint(t.getDestination().getLat(), t.getDestination().getLng()));
        }

        if (pts.size() >= 2) {
            routeLine = new Polyline();
            routeLine.setPoints(pts);
            routeLine.setWidth(10f);
            routeLine.setColor(ContextCompat.getColor(this, R.color.green));
            map.getOverlays().add(routeLine);
        }

        LatLngDto car = t.getCar();
        if (car != null) {
            carMarker = new Marker(map);
            carMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            carMarker.setTitle("Car");
            if (carIcon != null) carMarker.setIcon(carIcon);
            carMarker.setPosition(new GeoPoint(car.getLat(), car.getLng()));
            map.getOverlays().add(carMarker);
        }

        LatLngDto pickup = t.getPickup();
        if (pickup != null) {
            pickupMarker = new Marker(map);
            pickupMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            pickupMarker.setTitle("Pickup");
            if (pickupIcon != null) pickupMarker.setIcon(pickupIcon);
            pickupMarker.setPosition(new GeoPoint(pickup.getLat(), pickup.getLng()));
            map.getOverlays().add(pickupMarker);

            if (firstCamera) {
                map.getController().setCenter(new GeoPoint(pickup.getLat(), pickup.getLng()));
                map.getController().setZoom(14.5);
                firstCamera = false;
            }
        }

        LatLngDto dest = t.getDestination();
        if (dest != null) {
            destMarker = new Marker(map);
            destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            destMarker.setTitle("Destination");
            if (destIcon != null) destMarker.setIcon(destIcon);
            destMarker.setPosition(new GeoPoint(dest.getLat(), dest.getLng()));
            map.getOverlays().add(destMarker);
        }

        List<RideCheckpointDto> cps = t.getCheckpoints();
        if (cps != null && !cps.isEmpty()) {
            for (RideCheckpointDto c : cps) {
                if (c == null) continue;
                Marker m = new Marker(map);
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if (checkpointIcon != null) m.setIcon(checkpointIcon);
                m.setTitle("Stop " + c.getStopOrder());
                if (c.getAddress() != null && !c.getAddress().trim().isEmpty()) {
                    m.setSubDescription(c.getAddress());
                }
                m.setPosition(new GeoPoint(c.getLat(), c.getLng()));
                checkpointMarkers.add(m);
                map.getOverlays().add(m);
            }
        }

        map.invalidate();
    }

    private void clearRouteFromMap() {
        if (map == null) return;

        if (routeLine != null) {
            try { map.getOverlays().remove(routeLine); } catch (Exception ignore) {}
            routeLine = null;
        }

        if (carMarker != null) {
            try { map.getOverlays().remove(carMarker); } catch (Exception ignore) {}
            carMarker = null;
        }

        if (pickupMarker != null) {
            try { map.getOverlays().remove(pickupMarker); } catch (Exception ignore) {}
            pickupMarker = null;
        }

        if (destMarker != null) {
            try { map.getOverlays().remove(destMarker); } catch (Exception ignore) {}
            destMarker = null;
        }

        for (Marker m : checkpointMarkers) {
            try { map.getOverlays().remove(m); } catch (Exception ignore) {}
        }
        checkpointMarkers.clear();

        map.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        if (map != null) map.onPause();
        super.onPause();
    }
}
