package com.example.taximobile.feature.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.AdminRideStatusRepository;
import com.example.taximobile.feature.admin.data.dto.response.AdminRideStatusRowDto;

import java.util.ArrayList;
import java.util.List;

public class AdminRideStatusActivity extends AdminBaseActivity {

    private EditText etQuery;
    private Button btnSearch;

    private RecyclerView recycler;
    private TextView tvEmpty, tvError;

    private final ArrayList<AdminRideStatusRowDto> items = new ArrayList<>();
    private AdminRideStatusAdapter adapter;
    private AdminRideStatusRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_ride_status);
        toolbar.setTitle("Ride status");

        etQuery = v.findViewById(R.id.etQuery);
        btnSearch = v.findViewById(R.id.btnSearch);

        recycler = v.findViewById(R.id.recycler);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        tvError = v.findViewById(R.id.tvError);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRideStatusAdapter(items, this::openDetails);
        recycler.setAdapter(adapter);

        repo = new AdminRideStatusRepository(this);

        btnSearch.setOnClickListener(x -> load());
        load();
    }

    private void load() {
        showError(null);

        String q = etQuery.getText() != null ? etQuery.getText().toString().trim() : "";
        repo.list(q, 50, new AdminRideStatusRepository.ListCb() {
            @Override
            public void onSuccess(List<AdminRideStatusRowDto> list) {
                runOnUiThread(() -> {
                    items.clear();
                    if (list != null) items.addAll(list);
                    adapter.notifyDataSetChanged();
                    updateEmpty();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    showError(msg);
                    updateEmpty();
                });
            }
        });
    }

    private void openDetails(AdminRideStatusRowDto r) {
        Intent i = new Intent(this, AdminRideStatusDetailsActivity.class);

        i.putExtra("rideId", r.getRideId());
        i.putExtra("driverId", r.getDriverId());
        i.putExtra("userId", r.getUserId() != null ? r.getUserId() : -1L);

        i.putExtra("driverEmail", safe(r.getDriverEmail()));
        i.putExtra("driverFirstName", safe(r.getDriverFirstName()));
        i.putExtra("driverLastName", safe(r.getDriverLastName()));

        i.putExtra("status", safe(r.getStatus()));
        i.putExtra("startedAt", safe(r.getStartedAt()));

        i.putExtra("carLat", r.getCarLat());
        i.putExtra("carLng", r.getCarLng());

        startActivity(i);
    }

    private void updateEmpty() {
        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
