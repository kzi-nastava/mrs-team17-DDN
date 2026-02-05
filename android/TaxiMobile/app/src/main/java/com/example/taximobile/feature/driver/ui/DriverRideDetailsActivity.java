package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.View;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityDriverRideDetailsBinding;

public class DriverRideDetailsActivity extends DriverBaseActivity {

    private ActivityDriverRideDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_ride_details);
        binding = ActivityDriverRideDetailsBinding.bind(v);

        toolbar.setTitle(getString(R.string.title_ride_details));

        String dateStart = getIntent().getStringExtra("dateStart");
        String dateEnd = getIntent().getStringExtra("dateEnd");
        String route = getIntent().getStringExtra("route");
        int price = getIntent().getIntExtra("price", 0);
        String status = getIntent().getStringExtra("status");
        boolean panic = getIntent().getBooleanExtra("panic", false);

        binding.tvDateStart.setText(dateStart);
        binding.tvDateEnd.setText(dateEnd);
        binding.tvRoute.setText(route);
        binding.tvPrice.setText(price + " RSD");
        binding.tvStatus.setText(status);
        binding.tvPanic.setText(panic
                ? getString(R.string.panic_yes)
                : getString(R.string.panic_no));

        String passengerName = getIntent().getStringExtra("passengerName");
        String passengerEmail = getIntent().getStringExtra("passengerEmail");

        if (passengerName == null || passengerEmail == null) {
            binding.cardPassengers.setVisibility(View.GONE);
        } else {
            binding.cardPassengers.setVisibility(View.VISIBLE);
            binding.tvPassengerName.setText(passengerName);
            binding.tvPassengerEmail.setText(passengerEmail);
        }
    }
}
