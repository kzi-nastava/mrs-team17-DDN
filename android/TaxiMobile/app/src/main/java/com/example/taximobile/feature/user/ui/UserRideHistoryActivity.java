package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.adapter.UserRideHistoryAdapter;
import com.example.taximobile.feature.user.data.FavoriteRouteKeyUtil;
import com.example.taximobile.feature.user.data.FavoriteRouteRepository;
import com.example.taximobile.feature.user.data.UserRideHistoryRepository;
import com.example.taximobile.feature.user.data.dto.response.AddFavoriteFromRideResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;
import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class UserRideHistoryActivity extends UserBaseActivity {

    private final List<PassengerRideHistoryResponseDto> items = new ArrayList<>();
    private final Set<String> favoriteRouteKeys = new HashSet<>();
    private UserRideHistoryAdapter adapter;
    private UserRideHistoryRepository repo;
    private FavoriteRouteRepository favoriteRepo;

    private EditText etFrom;
    private EditText etTo;
    private ProgressBar progress;
    private TextView empty;
    private RecyclerView list;

    private String fromIso = null;
    private String toIso = null;
    private boolean initialDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_ride_history);
        toolbar.setTitle(getString(R.string.user_history_title));

        repo = new UserRideHistoryRepository(this);
        favoriteRepo = new FavoriteRouteRepository(this);

        etFrom = v.findViewById(R.id.etFrom);
        etTo = v.findViewById(R.id.etTo);
        progress = v.findViewById(R.id.urProgress);
        empty = v.findViewById(R.id.urEmpty);
        list = v.findViewById(R.id.urList);

        adapter = new UserRideHistoryAdapter(
                items,
                rideId -> {
                    Intent i = new Intent(this, PassengerRateRideActivity.class);
                    i.putExtra(PassengerRateRideActivity.EXTRA_RIDE_ID, rideId);
                    startActivity(i);
                },
                item -> {
                    if (item == null) return;
                    long rideId = item.getRideId() != null ? item.getRideId() : 0L;
                    Intent i = new Intent(this, UserRideDetailsActivity.class);
                    i.putExtra(UserRideDetailsActivity.EXTRA_RIDE_ID, rideId);
                    i.putExtra(UserRideDetailsActivity.EXTRA_STARTED_AT, item.getStartedAt());
                    i.putExtra(UserRideDetailsActivity.EXTRA_START_ADDRESS, item.getStartAddress());
                    i.putExtra(UserRideDetailsActivity.EXTRA_DEST_ADDRESS, item.getDestinationAddress());
                    if (item.getStops() != null && !item.getStops().isEmpty()) {
                        i.putStringArrayListExtra(
                                UserRideDetailsActivity.EXTRA_STOPS,
                                new java.util.ArrayList<>(item.getStops())
                        );
                    }
                    startActivity(i);
                },
                (rideId, position) -> addRideToFavorites(rideId, position),
                favoriteRouteKeys
        );
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        etFrom.setFocusable(false);
        etFrom.setClickable(true);
        etTo.setFocusable(false);
        etTo.setClickable(true);

        MaterialDatePicker<Pair<Long, Long>> rangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(getString(R.string.select_date_range))
                        .build();

        View.OnClickListener openPicker = vv ->
                rangePicker.show(getSupportFragmentManager(), "USER_RANGE_PICKER");

        etFrom.setOnClickListener(openPicker);
        etTo.setOnClickListener(openPicker);

        rangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;

            Long start = selection.first;
            Long end = selection.second;

            if (start != null) {
                etFrom.setText(formatUiDate(start));
                fromIso = formatIsoDate(start);
            }
            if (end != null) {
                etTo.setText(formatUiDate(end));
                toIso = formatIsoDate(end);
            }

            loadRides();
        });

        View btnFilter = v.findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(x -> loadRides());
        }

        loadFavoriteStateThenRides();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!initialDataLoaded) return;

        syncFavoriteState(() -> runOnUiThread(() -> adapter.notifyDataSetChanged()));
    }

    private void addRideToFavorites(long rideId, int position) {
        if (rideId <= 0) return;

        favoriteRepo.addFromRide(rideId, new FavoriteRouteRepository.AddCb() {
            @Override
            public void onSuccess(AddFavoriteFromRideResponseDto dto) {
                runOnUiThread(() -> {
                    addFavoriteKeyForRide(rideId, position);
                    if (position >= 0 && position < items.size()) {
                        adapter.notifyItemChanged(position);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(
                            UserRideHistoryActivity.this,
                            getString(R.string.favorite_added_msg),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 409) {
                        addFavoriteKeyForRide(rideId, position);
                        if (position >= 0 && position < items.size()) {
                            adapter.notifyItemChanged(position);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        Toast.makeText(
                                UserRideHistoryActivity.this,
                                getString(R.string.favorite_exists_msg),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Toast.makeText(
                            UserRideHistoryActivity.this,
                            msg != null ? msg : "Request failed",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void loadFavoriteStateThenRides() {
        setLoading(true);
        syncFavoriteState(this::loadRides);
    }

    private void syncFavoriteState(Runnable onDone) {
        favoriteRepo.listFavorites(new FavoriteRouteRepository.ListCb() {
            @Override
            public void onSuccess(List<FavoriteRouteResponseDto> favorites) {
                favoriteRouteKeys.clear();
                for (FavoriteRouteResponseDto f : favorites) {
                    favoriteRouteKeys.add(FavoriteRouteKeyUtil.fromFavoriteDto(f));
                }
                if (onDone != null) onDone.run();
            }

            @Override
            public void onError(String msg, int httpCode) {
                if (onDone != null) onDone.run();
            }
        });
    }

    private void addFavoriteKeyForRide(long rideId, int position) {
        PassengerRideHistoryResponseDto item = getRideItem(rideId, position);
        if (item == null) return;
        favoriteRouteKeys.add(FavoriteRouteKeyUtil.fromRideDto(item));
    }

    private PassengerRideHistoryResponseDto getRideItem(long rideId, int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }

        for (PassengerRideHistoryResponseDto d : items) {
            if (d != null && d.getRideId() != null && d.getRideId() == rideId) {
                return d;
            }
        }

        return null;
    }

    private void loadRides() {
        setLoading(true);
        repo.getRides(fromIso, toIso, new UserRideHistoryRepository.ListCb() {
            @Override
            public void onSuccess(List<PassengerRideHistoryResponseDto> listItems) {
                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(listItems);
                    adapter.notifyDataSetChanged();
                    initialDataLoaded = true;
                    setLoading(false);
                    empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(UserRideHistoryActivity.this, msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        list.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (loading) {
            empty.setVisibility(View.GONE);
        }
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
