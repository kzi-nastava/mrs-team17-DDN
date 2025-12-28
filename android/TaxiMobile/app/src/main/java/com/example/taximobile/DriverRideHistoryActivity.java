package com.example.taximobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taximobile.adapters.RideAdapter;
import com.example.taximobile.databinding.ActivityDriverRideHistoryBinding;
import com.example.taximobile.models.Ride;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DriverRideHistoryActivity extends DriverBaseActivity {

    private ActivityDriverRideHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_ride_history);
        binding = ActivityDriverRideHistoryBinding.bind(v);

        toolbar.setTitle(getString(R.string.title_ride_history));

        // Make date fields behave like picker inputs (no keyboard)
        binding.etFrom.setFocusable(false);
        binding.etFrom.setClickable(true);
        binding.etTo.setFocusable(false);
        binding.etTo.setClickable(true);

        // Date range picker (fills both From and To)
        MaterialDatePicker<Pair<Long, Long>> rangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select date range")
                        .build();

        View.OnClickListener openPicker = vv ->
                rangePicker.show(getSupportFragmentManager(), "RANGE_PICKER");

        binding.etFrom.setOnClickListener(openPicker);
        binding.etTo.setOnClickListener(openPicker);

        rangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;

            Long start = selection.first;
            Long end = selection.second;

            if (start != null) binding.etFrom.setText(formatDate(start));
            if (end != null) binding.etTo.setText(formatDate(end));
        });

        ArrayList<Ride> rides = new ArrayList<>();

        rides.add(new Ride(
                "13.12.2025 14:30",
                "13.12.2025 15:05",
                "FTN → Železnička",
                820,
                "Completed",
                false,
                "Marko Marković",
                "marko@gmail.com"
        ));

        rides.add(new Ride(
                "12.12.2025 09:15",
                "12.12.2025 09:45",
                "Limanski → Centar",
                650,
                "Completed",
                false,
                null,
                null
        ));

        rides.add(new Ride(
                "11.12.2025 21:40",
                "11.12.2025 21:55",
                "Aviv Park → Bulevar",
                900,
                "Cancelled",
                false,
                null,
                null
        ));

        rides.add(new Ride(
                "10.12.2025 17:05",
                "10.12.2025 17:25",
                "Spens → Telep",
                500,
                "Completed",
                true,
                "Ana Anić",
                "ana@gmail.com"
        ));

        binding.recyclerRides.setLayoutManager(new LinearLayoutManager(this));

        binding.recyclerRides.setAdapter(new RideAdapter(rides, ride -> {
            Intent i = new Intent(this, DriverRideDetailsActivity.class);

            i.putExtra("dateStart", ride.getDateStart());
            i.putExtra("dateEnd", ride.getDateEnd());
            i.putExtra("route", ride.getRoute());
            i.putExtra("price", ride.getPrice());
            i.putExtra("status", ride.getStatus());
            i.putExtra("panic", ride.isPanic());

            if (ride.getPassengerName() != null) {
                i.putExtra("passengerName", ride.getPassengerName());
                i.putExtra("passengerEmail", ride.getPassengerEmail());
            }

            startActivity(i);
        }));
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }
}
