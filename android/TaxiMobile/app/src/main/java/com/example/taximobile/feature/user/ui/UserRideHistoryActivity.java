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
import com.example.taximobile.feature.user.data.UserRideHistoryRepository;
import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserRideHistoryActivity extends UserBaseActivity {

    private final List<PassengerRideHistoryResponseDto> items = new ArrayList<>();
    private UserRideHistoryAdapter adapter;
    private UserRideHistoryRepository repo;

    private EditText etFrom;
    private EditText etTo;
    private ProgressBar progress;
    private TextView empty;
    private RecyclerView list;

    private String fromIso = null;
    private String toIso = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_ride_history);
        toolbar.setTitle(getString(R.string.user_history_title));

        repo = new UserRideHistoryRepository(this);

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
                }
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

        loadRides();
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
