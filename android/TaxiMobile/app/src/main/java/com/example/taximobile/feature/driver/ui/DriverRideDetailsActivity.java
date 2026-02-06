// app/src/main/java/com/example/taximobile/feature/driver/ui/DriverRideDetailsActivity.java
package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.View;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityDriverRideDetailsBinding;
import com.example.taximobile.feature.driver.data.DriverRideRepository;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.PassengerInfoResponseDto;

import java.util.List;

public class DriverRideDetailsActivity extends DriverBaseActivity {

    private ActivityDriverRideDetailsBinding binding;
    private DriverRideRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_ride_details);
        binding = ActivityDriverRideDetailsBinding.bind(v);

        toolbar.setTitle(getString(R.string.title_ride_details));

        repo = new DriverRideRepository(this);

        long rideId = getIntent().getLongExtra("rideId", -1L);
        if (rideId <= 0) {
            finish();
            return;
        }

        loadDetails(rideId);
    }

    private void loadDetails(long rideId) {
        repo.getRideDetails(rideId, new DriverRideRepository.DetailsCb() {
            @Override
            public void onSuccess(DriverRideDetailsResponseDto d) {
                runOnUiThread(() -> bindDetails(d));
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    // handle error
                });
            }
        });
    }

    private void bindDetails(DriverRideDetailsResponseDto d) {
        String route = safe(d.getStartAddress()) + " → " + safe(d.getDestinationAddress());

        binding.tvDateStart.setText(safe(d.getStartedAt()));
        binding.tvDateEnd.setText(safe(d.getEndedAt()));
        binding.tvRoute.setText(route);
        binding.tvPrice.setText(((int) Math.round(d.getPrice())) + " RSD");
        binding.tvStatus.setText(safe(d.getStatus()));
        binding.tvPanic.setText(d.isPanicTriggered()
                ? getString(R.string.panic_yes)
                : getString(R.string.panic_no));

        List<PassengerInfoResponseDto> passengers = d.getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            binding.cardPassengers.setVisibility(View.GONE);
        } else {
            binding.cardPassengers.setVisibility(View.VISIBLE);
            PassengerInfoResponseDto p0 = passengers.get(0);
            binding.tvPassengerName.setText(safe(p0.getName()));
            binding.tvPassengerEmail.setText(safe(p0.getEmail()));
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
