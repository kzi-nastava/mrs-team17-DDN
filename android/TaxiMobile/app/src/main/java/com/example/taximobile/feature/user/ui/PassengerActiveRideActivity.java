package com.example.taximobile.feature.user.ui;

import android.content.Intent;
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

    public static final String EXTRA_RIDE_ID = "extra_ride_id";
    public static final String EXTRA_READ_ONLY = "extra_read_only";
    public static final String EXTRA_NOTIFICATION_TYPE = "extra_notification_type";

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

    private boolean specificRideMode = false;
    private long selectedRideId = -1L;
    private boolean readOnlyMode = false;
    private String notificationType;

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private static final long POLL_MS = 2000L;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadTracking(false);
            if (shouldPoll()) {
                handler.postDelayed(this, POLL_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resolveModeFromIntent();

        View v = inflateContent(R.layout.activity_passenger_active_ride);
        toolbar.setTitle(getString(specificRideMode ? R.string.title_ride_tracking : R.string.title_active_ride));

        tvEta = v.findViewById(R.id.tvEta);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvError = v.findViewById(R.id.tvError);
        btnReport = v.findViewById(R.id.btnReport);
        map = v.findViewById(R.id.map);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        carIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_car);
        pickupIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_pickup);
        destIcon = ContextCompat.getDrawable(this, R.drawable.ic_marker_destination);

        repo = new RideActiveRepository(this);

        btnReport.setOnClickListener(x -> openReportDialog());
        if (readOnlyMode) {
            btnReport.setVisibility(View.GONE);
        }

        loadTracking(true);
    }

    private void resolveModeFromIntent() {
        Intent i = getIntent();
        if (i == null) return;

        selectedRideId = i.getLongExtra(EXTRA_RIDE_ID, -1L);
        specificRideMode = selectedRideId > 0;
        readOnlyMode = i.getBooleanExtra(EXTRA_READ_ONLY, false);
        notificationType = i.getStringExtra(EXTRA_NOTIFICATION_TYPE);

        if ("RIDE_FINISHED".equalsIgnoreCase(notificationType)) {
            readOnlyMode = true;
        }
    }

    private void loadTracking(boolean moveCamera) {
        if (specificRideMode) {
            loadTrackingByRideId(moveCamera);
            return;
        }

        repo.getTracking(new RideActiveRepository.TrackingCb() {
            @Override
            public void onSuccess(RideTrackingResponseDto dto) {
                runOnUiThread(() -> {
                    hasActiveRide = true;
                    setTrackingUi(true);

                    showError(null);
                    bindTrackingSummary(dto);
                    render(dto, moveCamera);
                });
            }

            @Override
            public void onNoActiveRide() {
                runOnUiThread(() -> {
                    hasActiveRide = false;
                    clearOverlays();
                    setNoRideUi();
                    showError(getString(R.string.tracking_no_active_ride));
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> showError(msg));
            }
        });
    }

    private void loadTrackingByRideId(boolean moveCamera) {
        repo.getTrackingByRideId(selectedRideId, new RideActiveRepository.TrackingByIdCb() {
            @Override
            public void onSuccess(RideTrackingResponseDto dto) {
                runOnUiThread(() -> {
                    if (dto == null) {
                        clearOverlays();
                        setNoRideUi();
                        showError(getString(R.string.tracking_load_failed));
                        return;
                    }

                    hasActiveRide = "ACTIVE".equalsIgnoreCase(dto.getStatus());
                    setTrackingUi(!readOnlyMode && hasActiveRide);

                    showError(null);
                    bindTrackingSummary(dto);
                    render(dto, moveCamera);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    clearOverlays();
                    setNoRideUi();
                    showError(msg);
                });
            }
        });
    }

    private void bindTrackingSummary(RideTrackingResponseDto dto) {
        tvEta.setText(String.format(
                Locale.US,
                getString(R.string.tracking_eta_fmt),
                dto.getEtaMinutes(),
                dto.getDistanceKm()
        ));
        tvStatus.setText(getString(R.string.tracking_status_fmt, safe(dto.getStatus())));
    }

    private void setTrackingUi(boolean reportEnabled) {
        map.setVisibility(View.VISIBLE);
        btnReport.setVisibility(reportEnabled ? View.VISIBLE : View.GONE);
    }

    private void setNoRideUi() {
        map.setVisibility(View.GONE);
        btnReport.setVisibility(View.GONE);
        tvEta.setText(getString(R.string.tracking_eta_empty));
        tvStatus.setText(getString(R.string.tracking_status_empty));
    }

    private void render(RideTrackingResponseDto dto, boolean moveCamera) {
        if (dto == null) return;

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
        if (readOnlyMode || !hasActiveRide) {
            showError(getString(R.string.tracking_report_inactive));
            return;
        }

        View form = getLayoutInflater().inflate(R.layout.dialog_report_issue, null, false);
        EditText et = form.findViewById(R.id.etReport);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tracking_report_title))
                .setView(form)
                .setNegativeButton(getString(R.string.tracking_report_cancel), (d, w) -> d.dismiss())
                .setPositiveButton(getString(R.string.tracking_report_send), (d, w) -> {
                    String text = et.getText() != null ? et.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(text)) return;
                    sendReport(text);
                })
                .show();
    }

    private void sendReport(String text) {
        repo.report(text, new RideActiveRepository.ReportCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.user.data.dto.response.RideReportResponseDto dto) {
                runOnUiThread(() -> showError(getString(R.string.tracking_report_sent)));
            }

            @Override
            public void onError(String msg) {
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

    private boolean shouldPoll() {
        return !readOnlyMode;
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();

        handler.removeCallbacks(pollRunnable);
        if (shouldPoll()) {
            handler.post(pollRunnable);
        }
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(pollRunnable);
        if (map != null) map.onPause();
        super.onPause();
    }
}
