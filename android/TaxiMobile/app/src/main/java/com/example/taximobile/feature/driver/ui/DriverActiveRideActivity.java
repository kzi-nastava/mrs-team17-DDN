package com.example.taximobile.feature.driver.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taximobile.R;
import com.example.taximobile.feature.driver.data.DriverRideRepository;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;

public class DriverActiveRideActivity extends DriverBaseActivity {

    private DriverRideRepository repo;

    private ProgressBar progress;
    private View emptyBox;
    private View contentBox;

    private TextView txtTitle;
    private TextView txtStatus;
    private TextView txtRoute;
    private TextView txtPrice;

    private Button btnReload;
    private Button btnFinish;

    private Long activeRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflateContent(R.layout.activity_driver_active_ride);

        toolbar.setTitle("Active ride");

        repo = new DriverRideRepository(this);

        progress = v.findViewById(R.id.arProgress);
        emptyBox = v.findViewById(R.id.arEmptyBox);
        contentBox = v.findViewById(R.id.arContentBox);

        txtTitle = v.findViewById(R.id.arTitle);
        txtStatus = v.findViewById(R.id.arStatus);
        txtRoute = v.findViewById(R.id.arRoute);
        txtPrice = v.findViewById(R.id.arPrice);

        btnReload = v.findViewById(R.id.arReload);
        btnFinish = v.findViewById(R.id.arFinish);

        btnReload.setOnClickListener(x -> load());
        btnFinish.setOnClickListener(x -> confirmFinish());

        load();
    }

    private void load() {
        setLoading(true);
        repo.getActiveRide(new DriverRideRepository.ActiveRideCb() {
            @Override
            public void onSuccess(DriverRideDetailsResponseDto dto) {
                activeRideId = dto.getRideId();
                render(dto);
                showContent(true);
                setLoading(false);
            }

            @Override
            public void onEmpty() {
                activeRideId = null;
                showContent(false);
                setLoading(false);
            }

            @Override
            public void onError(String msg) {
                activeRideId = null;
                showContent(false);
                setLoading(false);
                Toast.makeText(DriverActiveRideActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void render(DriverRideDetailsResponseDto dto) {
        txtTitle.setText("Ride #" + dto.getRideId());

        String status = dto.getStatus() != null ? dto.getStatus() : "-";
        txtStatus.setText("Status: " + status);

        String route = (dto.getStartAddress() != null ? dto.getStartAddress() : "-")
                + " → " +
                (dto.getDestinationAddress() != null ? dto.getDestinationAddress() : "-");
        txtRoute.setText(route);

        txtPrice.setText("Price: " + dto.getPrice());

        // Minimalno: zabrani finish ako je canceled ili već ended
        boolean canFinish = !dto.isCanceled() && dto.getEndedAt() == null;
        btnFinish.setEnabled(canFinish);
    }

    private void confirmFinish() {
        if (activeRideId == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Finish ride")
                .setMessage("Mark this ride as finished and paid?")
                .setPositiveButton("Finish", (d, w) -> doFinish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doFinish() {
        if (activeRideId == null) return;

        setLoading(true);
        btnFinish.setEnabled(false);

        repo.finishRide(activeRideId, new DriverRideRepository.FinishCb() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(DriverActiveRideActivity.this, "Ride finished", Toast.LENGTH_SHORT).show();
                load(); // posle finish očekuj prazno (nema active ride)
            }

            @Override
            public void onError(String msg) {
                setLoading(false);
                Toast.makeText(DriverActiveRideActivity.this, msg, Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReload.setEnabled(!loading);
    }

    private void showContent(boolean hasActive) {
        contentBox.setVisibility(hasActive ? View.VISIBLE : View.GONE);
        emptyBox.setVisibility(hasActive ? View.GONE : View.VISIBLE);
    }
}
