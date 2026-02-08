package com.example.taximobile.feature.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taximobile.R;
import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.databinding.ActivityAdminUpdateRequestsBinding;
import com.example.taximobile.feature.admin.data.AdminUpdateRequestsRepository;
import com.example.taximobile.feature.auth.ui.LoginActivity;
import com.example.taximobile.feature.driver.data.DriverApi;
import com.example.taximobile.feature.driver.data.dto.response.DriverProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUpdateRequestsActivity extends AdminBaseActivity implements AdminUpdateRequestsAdapter.Listener {

    private ActivityAdminUpdateRequestsBinding binding;

    private AdminUpdateRequestsRepository repo;
    private DriverApi driverApi;

    private AdminUpdateRequestsAdapter adapter;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_update_requests);
        binding = ActivityAdminUpdateRequestsBinding.bind(v);

        toolbar.setTitle(getString(R.string.menu_update_requests));

        repo = new AdminUpdateRequestsRepository(this);
        driverApi = ApiClient.get(getApplicationContext()).create(DriverApi.class);

        adapter = new AdminUpdateRequestsAdapter(this);
        binding.recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRequests.setAdapter(adapter);

        binding.btnRefresh.setOnClickListener(view -> load());

        load();
    }

    private void load() {
        if (isLoading) return;
        isLoading = true;

        setStateLoading(true);
        showState(null);

        repo.list("PENDING", new AdminUpdateRequestsRepository.ListCb() {
            @Override
            public void onSuccess(List<ProfileChangeRequestResponseDto> items) {
                runOnUiThread(() -> {
                    isLoading = false;
                    setStateLoading(false);

                    adapter.setItems(items);

                    if (items == null || items.isEmpty()) {
                        showState(getString(R.string.update_requests_empty));
                        return;
                    }

                    showState(null);
                    loadCurrentProfilesForDiff(items);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    isLoading = false;
                    setStateLoading(false);

                    if (httpCode == 401) {
                        new TokenStorage(getApplicationContext()).clear();
                        Toast.makeText(AdminUpdateRequestsActivity.this,
                                "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLogin();
                        return;
                    }

                    String text = getString(R.string.update_requests_load_failed)
                            + " (HTTP " + httpCode + ")"
                            + (msg != null && !msg.trim().isEmpty() ? ("\n" + msg) : "");

                    showState(text);
                });
            }
        });
    }

    private void loadCurrentProfilesForDiff(List<ProfileChangeRequestResponseDto> items) {
        if (items == null) return;

        Set<Long> uniqueDriverIds = new HashSet<>();
        for (ProfileChangeRequestResponseDto r : items) {
            if (r == null) continue;
            Long driverId = r.getDriverId();
            if (driverId != null) uniqueDriverIds.add(driverId);
        }

        for (Long driverId : uniqueDriverIds) {
            if (driverId == null) continue;

            driverApi.getDriverProfile(driverId).enqueue(new Callback<DriverProfileResponseDto>() {
                @Override
                public void onResponse(Call<DriverProfileResponseDto> call, Response<DriverProfileResponseDto> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        runOnUiThread(() -> adapter.setCurrentProfile(driverId, res.body()));
                    }
                }

                @Override
                public void onFailure(Call<DriverProfileResponseDto> call, Throwable t) {
                }
            });
        }
    }

    private void setStateLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRefresh.setEnabled(!loading);
    }

    private void showState(String text) {
        if (text == null || text.trim().isEmpty()) {
            binding.tvState.setVisibility(View.GONE);
            binding.tvState.setText("");
            return;
        }
        binding.tvState.setText(text);
        binding.tvState.setVisibility(View.VISIBLE);
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void onApproveClicked(ProfileChangeRequestResponseDto item) {
        if (item == null || item.getRequestId() == null) return;

        long requestId = item.getRequestId();
        adapter.setActing(requestId, true);

        repo.approve(requestId, null, new AdminUpdateRequestsRepository.ActionCb() {
            @Override
            public void onSuccess(ProfileChangeRequestResponseDto updated) {
                runOnUiThread(() -> {
                    adapter.setActing(requestId, false);
                    adapter.replaceItem(updated);
                    Toast.makeText(AdminUpdateRequestsActivity.this,
                            getString(R.string.update_requests_approved), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    adapter.setActing(requestId, false);

                    if (httpCode == 401) {
                        new TokenStorage(getApplicationContext()).clear();
                        Toast.makeText(AdminUpdateRequestsActivity.this,
                                "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLogin();
                        return;
                    }

                    Toast.makeText(AdminUpdateRequestsActivity.this,
                            getString(R.string.update_requests_action_failed)
                                    + " (HTTP " + httpCode + ")"
                                    + (msg != null && !msg.trim().isEmpty() ? ("\n" + msg) : ""),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onRejectClicked(ProfileChangeRequestResponseDto item) {
        if (item == null || item.getRequestId() == null) return;

        long requestId = item.getRequestId();

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint(getString(R.string.update_requests_reason_hint));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.update_requests_reject_title))
                .setView(input)
                .setNegativeButton(getString(R.string.update_requests_cancel), (d, w) -> d.dismiss())
                .setPositiveButton(getString(R.string.update_requests_reject), (d, w) -> {
                    String reason = input.getText() != null ? input.getText().toString().trim() : "";

                    adapter.setActing(requestId, true);

                    repo.reject(requestId, reason.isEmpty() ? null : reason, new AdminUpdateRequestsRepository.ActionCb() {
                        @Override
                        public void onSuccess(ProfileChangeRequestResponseDto updated) {
                            runOnUiThread(() -> {
                                adapter.setActing(requestId, false);
                                adapter.replaceItem(updated);
                                Toast.makeText(AdminUpdateRequestsActivity.this,
                                        getString(R.string.update_requests_rejected), Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String msg, int httpCode) {
                            runOnUiThread(() -> {
                                adapter.setActing(requestId, false);

                                if (httpCode == 401) {
                                    new TokenStorage(getApplicationContext()).clear();
                                    Toast.makeText(AdminUpdateRequestsActivity.this,
                                            "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                                    goToLogin();
                                    return;
                                }

                                Toast.makeText(AdminUpdateRequestsActivity.this,
                                        getString(R.string.update_requests_action_failed)
                                                + " (HTTP " + httpCode + ")"
                                                + (msg != null && !msg.trim().isEmpty() ? ("\n" + msg) : ""),
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .show();
    }
}
