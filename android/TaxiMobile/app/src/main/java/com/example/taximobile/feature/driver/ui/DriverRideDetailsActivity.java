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
        binding.tvCanceled.setText(d.isCanceled()
                ? getString(R.string.driver_ride_canceled_yes)
                : getString(R.string.driver_ride_canceled_no));
        binding.tvCanceledBy.setText(resolveCanceledBy(d.isCanceled(), d.getCanceledBy()));
        binding.tvPanic.setText(d.isPanicTriggered()
                ? getString(R.string.panic_yes)
                : getString(R.string.panic_no));

        List<PassengerInfoResponseDto> passengers = d.getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            binding.cardPassengers.setVisibility(View.GONE);
        } else {
            binding.cardPassengers.setVisibility(View.VISIBLE);
            StringBuilder names = new StringBuilder();
            StringBuilder emails = new StringBuilder();

            for (PassengerInfoResponseDto p : passengers) {
                if (p == null) continue;
                appendLine(names, safe(p.getName()));
                appendLine(emails, safe(p.getEmail()));
            }

            binding.tvPassengerName.setText(
                    names.length() > 0
                            ? names.toString()
                            : getString(R.string.driver_ride_passenger_unknown)
            );
            binding.tvPassengerEmail.setText(
                    emails.length() > 0
                            ? emails.toString()
                            : getString(R.string.driver_ride_passenger_unknown)
            );
        }
    }

    private String resolveCanceledBy(boolean canceled, String canceledBy) {
        if (!canceled) {
            return getString(R.string.driver_ride_canceled_by_na);
        }
        if (canceledBy == null || canceledBy.isBlank()) {
            return getString(R.string.driver_ride_canceled_by_unknown);
        }
        return canceledBy;
    }

    private void appendLine(StringBuilder out, String value) {
        if (value == null || value.isBlank()) return;
        if (out.length() > 0) out.append('\n');
        out.append(value);
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
