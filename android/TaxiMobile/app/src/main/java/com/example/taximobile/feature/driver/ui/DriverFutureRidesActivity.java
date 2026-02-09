package com.example.taximobile.feature.driver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.driver.adapter.RideAdapter;
import com.example.taximobile.feature.driver.data.DriverRideRepository;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideHistoryResponseDto;
import com.example.taximobile.feature.driver.model.Ride;

import java.util.ArrayList;
import java.util.List;

public class DriverFutureRidesActivity extends DriverBaseActivity {

    private DriverRideRepository repo;

    private ProgressBar progress;
    private TextView txtEmpty;
    private TextView txtError;
    private Button btnReload;
    private RecyclerView recycler;

    private final ArrayList<Ride> rides = new ArrayList<>();
    private RideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflateContent(R.layout.activity_driver_future_rides);
        toolbar.setTitle(getString(R.string.driver_future_rides_title));

        repo = new DriverRideRepository(this);

        progress = v.findViewById(R.id.dfrProgress);
        txtEmpty = v.findViewById(R.id.dfrEmpty);
        txtError = v.findViewById(R.id.dfrError);
        btnReload = v.findViewById(R.id.dfrReload);
        recycler = v.findViewById(R.id.dfrRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rides, ride -> {
            Intent intent = new Intent(this, DriverRideDetailsActivity.class);
            intent.putExtra("rideId", ride.getRideId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        btnReload.setOnClickListener(x -> loadRides());

        loadRides();
    }

    private void loadRides() {
        setLoading(true);
        showError(null);

        repo.getRides(null, null, new DriverRideRepository.ListCb() {
            @Override
            public void onSuccess(List<DriverRideHistoryResponseDto> incoming) {
                runOnUiThread(() -> {
                    rides.clear();

                    if (incoming != null) {
                        for (DriverRideHistoryResponseDto dto : incoming) {
                            if (dto == null) continue;
                            if (!isUpcomingStatus(dto.getStatus())) continue;
                            rides.add(toRide(dto));
                        }
                    }

                    adapter.notifyDataSetChanged();
                    setLoading(false);
                    showEmptyState(rides.isEmpty());
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    rides.clear();
                    adapter.notifyDataSetChanged();
                    setLoading(false);
                    showEmptyState(true);
                    showError(getString(R.string.driver_future_rides_error_load, msg));
                });
            }
        });
    }

    private boolean isUpcomingStatus(String status) {
        if (status == null) return false;
        return "SCHEDULED".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status);
    }

    private Ride toRide(DriverRideHistoryResponseDto dto) {
        String route = safe(dto.getStartAddress()) + " → " + safe(dto.getEndAddress());
        String date = safe(dto.getStartedAt());
        if (date.isEmpty()) date = "-";

        Ride ride = new Ride(
                date,
                "",
                route,
                (int) Math.round(dto.getPrice()),
                safeStatus(dto.getStatus()),
                false
        );
        ride.setRideId(dto.getRideId() != null ? dto.getRideId() : 0L);
        return ride;
    }

    private String safeStatus(String value) {
        return value != null && !value.trim().isEmpty() ? value : "ACCEPTED";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReload.setEnabled(!loading);
    }

    private void showEmptyState(boolean show) {
        txtEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            txtError.setVisibility(View.GONE);
        } else {
            txtError.setText(msg);
            txtError.setVisibility(View.VISIBLE);
        }
    }
}
