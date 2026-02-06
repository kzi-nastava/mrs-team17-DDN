package com.example.taximobile.feature.publichome.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taximobile.R;
import com.example.taximobile.feature.auth.ui.LoginActivity;
import com.example.taximobile.feature.publichome.data.VehiclesRepository;
import com.example.taximobile.feature.publichome.data.dto.response.ActiveVehicleResponseDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class PublicHomeMapActivity extends AppCompatActivity {

    private MapView map;
    private VehiclesRepository repo;

    private final List<Marker> markers = new ArrayList<>();

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final long REFRESH_MS = 2000; // 2s
    private final Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            loadVehicles();
            handler.postDelayed(this, REFRESH_MS);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // required by osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_public_home_map);

        map = findViewById(R.id.map);
        repo = new VehiclesRepository(this);

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        initMap();
        loadVehicles();
    }

    private void initMap() {
        map.setMultiTouchControls(true);

        // Novi Sad default
        GeoPoint ns = new GeoPoint(45.2671, 19.8335);
        map.getController().setZoom(13.5);
        map.getController().setCenter(ns);
    }

    private void loadVehicles() {
        // bez bbox filtera za sada (po specifikaciji: sva aktivna vozila)
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
                    updateStatsError(msg);
                });
            }
        });
    }

    private void renderMarkers(List<ActiveVehicleResponseDto> items) {
        // remove old
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
        for (ActiveVehicleResponseDto v : items) if (v.isBusy()) busy++;

        int free = items.size() - busy;

        ((android.widget.TextView) findViewById(R.id.tvStats)).setText(
                getString(R.string.vehicles_stats, free, busy)
        );
    }

    private void updateStatsError(String msg) {
        ((android.widget.TextView) findViewById(R.id.tvStats)).setText(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        handler.post(refreshRunnable);

    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refreshRunnable);
        map.onPause();
        super.onPause();
    }
}
