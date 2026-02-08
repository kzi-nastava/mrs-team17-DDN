package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.FavoriteRouteKeyUtil;
import com.example.taximobile.feature.user.data.FavoriteRouteRepository;
import com.example.taximobile.feature.user.data.dto.response.AddFavoriteFromRideResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;

import java.util.ArrayList;

public class UserRideDetailsActivity extends UserBaseActivity {

    public static final String EXTRA_RIDE_ID = "extra_ride_id";
    public static final String EXTRA_STARTED_AT = "extra_started_at";
    public static final String EXTRA_START_ADDRESS = "extra_start_address";
    public static final String EXTRA_DEST_ADDRESS = "extra_dest_address";
    public static final String EXTRA_STOPS = "extra_stops";

    private TextView tvRideId;
    private TextView tvDate;
    private TextView tvStart;
    private TextView tvDestination;
    private TextView tvStops;
    private ImageButton btnFavorite;
    private Button btnRate;

    private long rideId;
    private boolean isFavorited;
    private FavoriteRouteRepository favoriteRepo;
    private String rideRouteKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_ride_details);
        toolbar.setTitle(getString(R.string.user_ride_details_title));

        tvRideId = v.findViewById(R.id.tvRideId);
        tvDate = v.findViewById(R.id.tvRideDate);
        tvStart = v.findViewById(R.id.tvRideStart);
        tvDestination = v.findViewById(R.id.tvRideDestination);
        tvStops = v.findViewById(R.id.tvRideStops);
        btnFavorite = v.findViewById(R.id.btnFavoriteRide);
        btnRate = v.findViewById(R.id.btnRateRide);
        favoriteRepo = new FavoriteRouteRepository(this);

        Intent intent = getIntent();
        rideId = intent.getLongExtra(EXTRA_RIDE_ID, 0L);
        String startedAt = intent.getStringExtra(EXTRA_STARTED_AT);
        String startAddress = intent.getStringExtra(EXTRA_START_ADDRESS);
        String destAddress = intent.getStringExtra(EXTRA_DEST_ADDRESS);
        ArrayList<String> stops = intent.getStringArrayListExtra(EXTRA_STOPS);
        rideRouteKey = FavoriteRouteKeyUtil.fromRide(startAddress, destAddress, stops);

        tvRideId.setText(getString(R.string.ride_label, rideId));
        tvDate.setText(formatDateTime(startedAt));
        tvStart.setText(safe(startAddress));
        tvDestination.setText(safe(destAddress));

        if (stops == null || stops.isEmpty()) {
            tvStops.setVisibility(View.GONE);
        } else {
            tvStops.setVisibility(View.VISIBLE);
            tvStops.setText(joinStops(stops));
        }

        setFavoriteUi(false);
        btnFavorite.setOnClickListener(x -> addToFavorites());
        loadFavoriteState();

        btnRate.setOnClickListener(x -> {
            if (rideId <= 0) return;
            Intent i = new Intent(this, PassengerRateRideActivity.class);
            i.putExtra(PassengerRateRideActivity.EXTRA_RIDE_ID, rideId);
            startActivity(i);
        });
    }

    private void addToFavorites() {
        if (rideId <= 0 || isFavorited) return;

        favoriteRepo.addFromRide(rideId, new FavoriteRouteRepository.AddCb() {
            @Override
            public void onSuccess(AddFavoriteFromRideResponseDto dto) {
                runOnUiThread(() -> {
                    setFavoriteUi(true);
                    Toast.makeText(
                            UserRideDetailsActivity.this,
                            getString(R.string.favorite_added_msg),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 409) {
                        setFavoriteUi(true);
                        Toast.makeText(
                                UserRideDetailsActivity.this,
                                getString(R.string.favorite_exists_msg),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Toast.makeText(
                            UserRideDetailsActivity.this,
                            msg != null ? msg : "Request failed",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void loadFavoriteState() {
        favoriteRepo.listFavorites(new FavoriteRouteRepository.ListCb() {
            @Override
            public void onSuccess(java.util.List<FavoriteRouteResponseDto> items) {
                boolean found = false;
                for (FavoriteRouteResponseDto f : items) {
                    if (rideRouteKey.equals(FavoriteRouteKeyUtil.fromFavoriteDto(f))) {
                        found = true;
                        break;
                    }
                }
                final boolean finalFound = found;
                runOnUiThread(() -> setFavoriteUi(finalFound));
            }

            @Override
            public void onError(String msg, int httpCode) {
                // Keep default UI state when favorites cannot be loaded.
            }
        });
    }

    private void setFavoriteUi(boolean favorited) {
        isFavorited = favorited;
        btnFavorite.setEnabled(!favorited && rideId > 0);
        btnFavorite.setImageResource(
                favorited ? R.drawable.ic_star_filled_user : R.drawable.ic_star_outline_user
        );
        btnFavorite.setContentDescription(
                getString(favorited ? R.string.favorite_added_cd : R.string.favorite_add_cd)
        );
    }

    private static String safe(String s) {
        return s != null && !s.trim().isEmpty() ? s : "-";
    }

    private static String joinStops(ArrayList<String> stops) {
        if (stops == null || stops.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(stops.get(i));
        }
        return sb.toString();
    }

    private static String formatDateTime(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "-";
        String s = iso.trim();
        if (s.length() >= 16 && s.contains("T")) {
            String date = s.substring(0, 10); // yyyy-MM-dd
            String time = s.substring(11, 16); // HH:mm
            String[] p = date.split("-");
            if (p.length == 3) {
                return p[2] + "." + p[1] + "." + p[0] + " " + time;
            }
            return date + " " + time;
        }
        return s;
    }
}
