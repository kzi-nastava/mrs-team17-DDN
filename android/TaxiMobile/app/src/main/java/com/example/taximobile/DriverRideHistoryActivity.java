package com.example.taximobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taximobile.adapters.RideAdapter;
import com.example.taximobile.databinding.ActivityDriverRideHistoryBinding;
import com.example.taximobile.models.Ride;

import java.util.ArrayList;

public class DriverRideHistoryActivity extends DriverBaseActivity {

    private ActivityDriverRideHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_ride_history);
        binding = ActivityDriverRideHistoryBinding.bind(v);

        toolbar.setTitle(getString(R.string.title_ride_history));

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
}
