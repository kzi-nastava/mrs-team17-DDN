package com.example.taximobile.feature.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.RideRatingRepository;
import com.example.taximobile.feature.user.data.dto.response.RideRatingResponseDto;

public class PassengerRateRideActivity extends UserBaseActivity {

    public static final String EXTRA_RIDE_ID = "extra_ride_id";

    private RideRatingRepository repo;
    private Long rideId;

    private TextView tvRide;
    private RatingBar rbDriver;
    private RatingBar rbVehicle;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progress;
    private TextView tvError;
    private TextView tvSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_passenger_rate_ride);
        toolbar.setTitle(getString(R.string.rate_ride_title));

        repo = new RideRatingRepository(this);

        tvRide = v.findViewById(R.id.tvRide);
        rbDriver = v.findViewById(R.id.rbDriver);
        rbVehicle = v.findViewById(R.id.rbVehicle);
        etComment = v.findViewById(R.id.etComment);
        btnSubmit = v.findViewById(R.id.btnSubmitRating);
        progress = v.findViewById(R.id.ratingProgress);
        tvError = v.findViewById(R.id.tvRatingError);
        tvSuccess = v.findViewById(R.id.tvRatingSuccess);

        btnSubmit.setOnClickListener(x -> submit());

        long extraRideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1L);
        if (extraRideId > 0) {
            checkExisting(extraRideId);
        } else {
            loadPending();
        }
    }

    private void checkExisting(long rideId) {
        setLoading(true);
        repo.getRating(rideId, new RideRatingRepository.GetCb() {
            @Override
            public void onFound(RideRatingResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);
                    setAlreadyRated(rideId);
                });
            }

            @Override
            public void onNotFound() {
                runOnUiThread(() -> {
                    setLoading(false);
                    setRideId(rideId);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(msg);
                    setRideId(rideId);
                });
            }
        });
    }

    private void loadPending() {
        setLoading(true);
        repo.getPending(new RideRatingRepository.PendingCb() {
            @Override
            public void onSuccess(long rideId) {
                runOnUiThread(() -> {
                    setRideId(rideId);
                    setLoading(false);
                });
            }

            @Override
            public void onEmpty() {
                runOnUiThread(() -> {
                    setNoPending();
                    setLoading(false);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setNoPending();
                    showError(msg);
                    setLoading(false);
                });
            }
        });
    }

    private void setRideId(long id) {
        this.rideId = id;
        tvRide.setText(getString(R.string.rate_ride_label, id));
        setFormEnabled(true);
        rbDriver.setRating(5f);
        rbVehicle.setRating(5f);
    }

    private void setAlreadyRated(long id) {
        this.rideId = id;
        tvRide.setText(getString(R.string.rate_ride_label, id));
        setFormEnabled(false);
        showSuccess(getString(R.string.rate_ride_already_rated));
    }

    private void setNoPending() {
        this.rideId = null;
        tvRide.setText(getString(R.string.rate_ride_none));
        setFormEnabled(false);
    }

    private void setFormEnabled(boolean enabled) {
        rbDriver.setIsIndicator(!enabled);
        rbVehicle.setIsIndicator(!enabled);
        etComment.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);
    }

    private void submit() {
        if (rideId == null) {
            Toast.makeText(this, getString(R.string.rate_ride_none), Toast.LENGTH_SHORT).show();
            return;
        }

        clearMessages();

        int driverRating = Math.round(rbDriver.getRating());
        int vehicleRating = Math.round(rbVehicle.getRating());

        if (driverRating < 1 || vehicleRating < 1) {
            showError(getString(R.string.rate_ride_error_missing));
            return;
        }

        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        if (TextUtils.isEmpty(comment)) comment = null;

        setLoading(true);
        repo.submitRating(rideId, driverRating, vehicleRating, comment, new RideRatingRepository.SubmitCb() {
            @Override
            public void onSuccess(RideRatingResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSuccess(getString(R.string.rate_ride_success));
                    finish();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(msg);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading && rideId != null);
        rbDriver.setIsIndicator(loading || rideId == null);
        rbVehicle.setIsIndicator(loading || rideId == null);
        etComment.setEnabled(!loading && rideId != null);
    }

    private void clearMessages() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }

    private void showSuccess(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        tvSuccess.setText(msg);
        tvSuccess.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }
}
