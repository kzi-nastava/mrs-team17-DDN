// app/src/main/java/com/example/taximobile/feature/driver/ui/DriverRideHistoryActivity.java
package com.example.taximobile.feature.driver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityDriverRideHistoryBinding;
import com.example.taximobile.feature.driver.adapter.RideAdapter;
import com.example.taximobile.feature.driver.data.DriverRideRepository;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideHistoryResponseDto;
import com.example.taximobile.feature.driver.model.Ride;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DriverRideHistoryActivity extends DriverBaseActivity {

    private ActivityDriverRideHistoryBinding binding;

    private final ArrayList<Ride> rides = new ArrayList<>();
    private RideAdapter adapter;

    private DriverRideRepository repo;

    private String fromIso = null; // yyyy-MM-dd
    private String toIso = null;   // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_ride_history);
        binding = ActivityDriverRideHistoryBinding.bind(v);

        toolbar.setTitle(getString(R.string.title_ride_history));

        repo = new DriverRideRepository(this);

        binding.recyclerRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rides, ride -> {
            Intent i = new Intent(this, DriverRideDetailsActivity.class);
            i.putExtra("rideId", ride.getRideId());
            startActivity(i);
        });
        binding.recyclerRides.setAdapter(adapter);

        binding.etFrom.setFocusable(false);
        binding.etFrom.setClickable(true);
        binding.etTo.setFocusable(false);
        binding.etTo.setClickable(true);

        MaterialDatePicker<Pair<Long, Long>> rangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(getString(R.string.select_date_range))
                        .build();

        View.OnClickListener openPicker = vv ->
                rangePicker.show(getSupportFragmentManager(), "RANGE_PICKER");

        binding.etFrom.setOnClickListener(openPicker);
        binding.etTo.setOnClickListener(openPicker);

        rangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;

            Long start = selection.first;
            Long end = selection.second;

            if (start != null) {
                binding.etFrom.setText(formatUiDate(start));
                fromIso = formatIsoDate(start);
            }
            if (end != null) {
                binding.etTo.setText(formatUiDate(end));
                toIso = formatIsoDate(end);
            }

            loadRides();
        });

        loadRides();
    }

    private void loadRides() {
        repo.getRides(fromIso, toIso, new DriverRideRepository.ListCb() {
            @Override
            public void onSuccess(java.util.List<DriverRideHistoryResponseDto> items) {
                runOnUiThread(() -> {
                    rides.clear();

                    for (DriverRideHistoryResponseDto d : items) {
                        String route = safe(d.getStartAddress()) + " → " + safe(d.getEndAddress());

                        String status = d.isCanceled()
                                ? "Cancelled"
                                : safe(d.getStatus());

                        Ride r = new Ride(
                                safe(d.getStartedAt()),
                                "",
                                route,
                                (int) Math.round(d.getPrice()),
                                status,
                                false
                        );

                        r.setRideId(d.getRideId() != null ? d.getRideId() : 0L);
                        rides.add(r);
                    }

                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    // show error if you have a view for it
                });
            }
        });
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private String formatUiDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }

    private String formatIsoDate(long millis) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        iso.setTimeZone(TimeZone.getDefault());
        return iso.format(new Date(millis));
    }
}
