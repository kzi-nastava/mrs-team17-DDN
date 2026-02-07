package com.example.taximobile.feature.user.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.RideActiveRepository;
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

public class PassengerActiveRideActivity extends UserBaseActivity {

    private TextView tvEta, tvStatus, tvError;
    private Button btnReport;
    private MapView map;

    private RideActiveRepository repo;

    private Marker carMarker;
    private Marker pickupMarker;
    private Marker destMarker;

    private Drawable carIcon;
    private Drawable pickupIcon;
    private Drawable destIcon;

    private final List<Marker> checkpointMarkers = new ArrayList<>();
    private Polyline routeLine;

    private boolean firstCamera = true;
    private boolean hasActiveRide = true;

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final long POLL_MS = 2000;

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            loadTracking(false);
            handler.postDelayed(this, POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_passenger_active_ride);
        toolbar.setTitle("Active ride");

        tvEta = v.findViewById(R.id.tvEta);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvError = v.findViewById(R.id.tvError);
        btnReport = v.findViewById(R.id.btnReport);
        map = v.findViewById(R.id.map);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // different icons
        carIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_car);
        pickupIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_pickup);
        destIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_destination);

        repo = new RideActiveRepository(this);

        btnReport.setOnClickListener(x -> openReportDialog());

        loadTracking(true);
    }

    private void loadTracking(boolean moveCamera) {
        repo.getTracking(new RideActiveRepository.TrackingCb() {
            @Override public void onSuccess(RideTrackingResponseDto dto) {
                runOnUiThread(() -> {
                    hasActiveRide = true;
                    setActiveRideUi(true);

                    showError(null);
                    tvEta.setText(String.format(Locale.US, "ETA: %d min (%.2f km)",
                            dto.getEtaMinutes(), dto.getDistanceKm()));
                    tvStatus.setText("Status: " + safe(dto.getStatus()));

                    render(dto, moveCamera);
                });
            }

            @Override public void onNoActiveRide() {
                runOnUiThread(() -> {
                    hasActiveRide = false;
                    clearOverlays();
                    setActiveRideUi(false);
                    showError("No active ride" );
                });
            }

            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    // ako nema vožnje, backend često vraća 404; to hvatamo gore.
                    showError(msg);
                });
            }
        });
    }

    private void setActiveRideUi(boolean active) {
        // ETA/Status može ostati, ali mapa i report nema smisla kad nema vožnje
        map.setVisibility(active ? View.VISIBLE : View.GONE);
        btnReport.setVisibility(active ? View.VISIBLE : View.GONE);

        if (!active) {
            tvEta.setText("ETA: -");
            tvStatus.setText("Status: -");
        }
    }

    private void render(RideTrackingResponseDto dto, boolean moveCamera) {
        if (dto == null) return;

        // car
        LatLngDto car = dto.getCar();
        if (car != null) {
            GeoPoint p = new GeoPoint(car.getLat(), car.getLng());
            if (carMarker == null) {
                carMarker = new Marker(map);
                carMarker.setTitle("Car");
                carMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if (carIcon != null) carMarker.setIcon(carIcon);
                map.getOverlays().add(carMarker);
            }
            carMarker.setPosition(p);

            if (firstCamera || moveCamera) {
                map.getController().setCenter(p);
                firstCamera = false;
            }
        }

        // pickup
        LatLngDto pickup = dto.getPickup();
        if (pickup != null) {
            GeoPoint p = new GeoPoint(pickup.getLat(), pickup.getLng());
            if (pickupMarker == null) {
                pickupMarker = new Marker(map);
                pickupMarker.setTitle("Pickup");
                pickupMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if (pickupIcon != null) pickupMarker.setIcon(pickupIcon);
                map.getOverlays().add(pickupMarker);
            }
            pickupMarker.setPosition(p);
        }

        // destination
        LatLngDto dest = dto.getDestination();
        if (dest != null) {
            GeoPoint p = new GeoPoint(dest.getLat(), dest.getLng());
            if (destMarker == null) {
                destMarker = new Marker(map);
                destMarker.setTitle("Destination");
                destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if (destIcon != null) destMarker.setIcon(destIcon);
                map.getOverlays().add(destMarker);
            }
            destMarker.setPosition(p);
        }

        // route polyline
        List<LatLngDto> route = dto.getRoute();
        if (route != null && !route.isEmpty()) {
            List<GeoPoint> pts = new ArrayList<>();
            for (LatLngDto pt : route) pts.add(new GeoPoint(pt.getLat(), pt.getLng()));

            if (routeLine != null) map.getOverlays().remove(routeLine);

            routeLine = new Polyline();
            routeLine.setPoints(pts);
            map.getOverlays().add(routeLine);
        } else {
            if (routeLine != null) {
                map.getOverlays().remove(routeLine);
                routeLine = null;
            }
        }

        // checkpoints markers
        clearCheckpointMarkers();
        List<RideCheckpointDto> cps = dto.getCheckpoints();
        if (cps != null && !cps.isEmpty()) {
            for (RideCheckpointDto c : cps) {
                Marker m = new Marker(map);
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                m.setPosition(new GeoPoint(c.getLat(), c.getLng()));
                m.setTitle("Stop " + c.getStopOrder());
                String addr = c.getAddress() != null ? c.getAddress() : "";
                if (!addr.isEmpty()) m.setSubDescription(addr);
                checkpointMarkers.add(m);
                map.getOverlays().add(m);
            }
        }

        map.invalidate();
    }

    private void clearOverlays() {
        if (carMarker != null) map.getOverlays().remove(carMarker);
        if (pickupMarker != null) map.getOverlays().remove(pickupMarker);
        if (destMarker != null) map.getOverlays().remove(destMarker);
        carMarker = pickupMarker = destMarker = null;

        if (routeLine != null) map.getOverlays().remove(routeLine);
        routeLine = null;

        clearCheckpointMarkers();
        map.invalidate();
    }

    private void clearCheckpointMarkers() {
        for (Marker m : checkpointMarkers) map.getOverlays().remove(m);
        checkpointMarkers.clear();
    }

    private void openReportDialog() {
        if (!hasActiveRide) {
            showError("Nema aktivne vožnje");
            return;
        }

        View form = getLayoutInflater().inflate(R.layout.dialog_report_issue, null, false);
        EditText et = form.findViewById(R.id.etReport);

        new AlertDialog.Builder(this)
                .setTitle("Report driver inconsistency")
                .setView(form)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Send", (d, w) -> {
                    String text = et.getText() != null ? et.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(text)) return;
                    sendReport(text);
                })
                .show();
    }

    private void sendReport(String text) {
        repo.report(text, new RideActiveRepository.ReportCb() {
            @Override public void onSuccess(com.example.taximobile.feature.user.data.dto.response.RideReportResponseDto dto) {
                runOnUiThread(() -> showError("Report sent"));
            }

            @Override public void onError(String msg) {
                runOnUiThread(() -> showError(msg));
            }
        });
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    @Override protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        handler.post(pollRunnable);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(pollRunnable);
        if (map != null) map.onPause();
        super.onPause();
    }
}
