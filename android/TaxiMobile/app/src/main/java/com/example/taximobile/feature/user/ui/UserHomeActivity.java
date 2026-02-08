package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import com.example.taximobile.R;
import com.example.taximobile.feature.publichome.data.VehiclesRepository;
import com.example.taximobile.feature.publichome.data.dto.response.ActiveVehicleResponseDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends UserBaseActivity {

    private MapView map;
    private TextView tvStats;

    private VehiclesRepository repo;

    private final List<Marker> markers = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_MS = 2000;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadVehicles();
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        android.view.View contentView = inflateContent(R.layout.activity_home_user);
        toolbar.setTitle(getString(R.string.menu_home));

        map = contentView.findViewById(R.id.mapHome);
        tvStats = contentView.findViewById(R.id.tvStats);

        Button btnOrder = contentView.findViewById(R.id.btnOrderRide);
        btnOrder.setOnClickListener(v ->
                startActivity(new Intent(UserHomeActivity.this, UserOrderRideActivity.class))
        );

        repo = new VehiclesRepository(this);

        initMap();
        loadVehicles();
    }

    private void initMap() {
        map.setMultiTouchControls(true);

        GeoPoint ns = new GeoPoint(45.2671, 19.8335);
        map.getController().setZoom(13.5);
        map.getController().setCenter(ns);
    }

    private void loadVehicles() {
        if (tvStats != null) {
            tvStats.setText(getString(R.string.vehicles_loading));
        }

        repo.getActiveVehicles(null, null, null, null, new VehiclesRepository.ListCb() {
            @Override
            public void onSuccess(List<ActiveVehicleResponseDto> items) {
                runOnUiThread(() -> {
                    renderMarkers(items);
                    updateStats(items);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    if (tvStats != null) tvStats.setText(msg);
                });
            }
        });
    }

    private void renderMarkers(List<ActiveVehicleResponseDto> items) {
        for (Marker m : markers) {
            map.getOverlays().remove(m);
        }
        markers.clear();

        for (ActiveVehicleResponseDto v : items) {
            Marker m = new Marker(map);
            m.setPosition(new GeoPoint(v.getLatitude(), v.getLongitude()));
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            String status = v.isBusy() ? getString(R.string.vehicle_busy) : getString(R.string.vehicle_free);
            m.setTitle("Vehicle #" + v.getId());
            m.setSubDescription(status);

            int iconRes = v.isBusy() ? R.drawable.ic_car_busy : R.drawable.ic_car_free;
            m.setIcon(androidx.core.content.ContextCompat.getDrawable(this, iconRes));

            markers.add(m);
            map.getOverlays().add(m);
        }

        map.invalidate();
    }

    private void updateStats(List<ActiveVehicleResponseDto> items) {
        int busy = 0;
        for (ActiveVehicleResponseDto v : items) {
            if (v.isBusy()) busy++;
        }
        int free = items.size() - busy;

        if (tvStats != null) {
            tvStats.setText(getString(R.string.vehicles_stats, free, busy));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refreshRunnable);
        if (map != null) map.onPause();
        super.onPause();
    }
}
