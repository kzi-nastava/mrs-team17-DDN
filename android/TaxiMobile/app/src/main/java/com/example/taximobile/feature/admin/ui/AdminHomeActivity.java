package com.example.taximobile.feature.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.admin.data.AdminUsersRepository;
import com.example.taximobile.feature.admin.data.dto.response.AdminUserStatusResponseDto;
import com.example.taximobile.feature.auth.ui.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AdminBaseActivity {

    private Button btnRefresh;
    private TextView tvGlobalError;

    private TextView tvDriversState;
    private RecyclerView rvDrivers;

    private TextView tvPassengersState;
    private RecyclerView rvPassengers;

    private final ArrayList<AdminUserStatusResponseDto> drivers = new ArrayList<>();
    private final ArrayList<AdminUserStatusResponseDto> passengers = new ArrayList<>();

    private AdminUserStatusAdapter driversAdapter;
    private AdminUserStatusAdapter passengersAdapter;

    private AdminUsersRepository repo;

    private boolean loadingDrivers = false;
    private boolean loadingPassengers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_user_blocking);
        toolbar.setTitle(getString(R.string.menu_home));

        btnRefresh = v.findViewById(R.id.btnRefresh);
        tvGlobalError = v.findViewById(R.id.tvGlobalError);

        tvDriversState = v.findViewById(R.id.tvDriversState);
        rvDrivers = v.findViewById(R.id.rvDrivers);

        tvPassengersState = v.findViewById(R.id.tvPassengersState);
        rvPassengers = v.findViewById(R.id.rvPassengers);

        repo = new AdminUsersRepository(this);

        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        rvPassengers.setLayoutManager(new LinearLayoutManager(this));

        driversAdapter = new AdminUserStatusAdapter(drivers, new AdminUserStatusAdapter.Actions() {
            @Override
            public void onOpenDialog(AdminUserStatusResponseDto user, boolean initialBlocked) {
                openBlockDialog(user, initialBlocked);
            }

            @Override
            public void onQuickUnblock(AdminUserStatusResponseDto user) {
                quickUnblock(user);
            }
        });

        passengersAdapter = new AdminUserStatusAdapter(passengers, new AdminUserStatusAdapter.Actions() {
            @Override
            public void onOpenDialog(AdminUserStatusResponseDto user, boolean initialBlocked) {
                openBlockDialog(user, initialBlocked);
            }

            @Override
            public void onQuickUnblock(AdminUserStatusResponseDto user) {
                quickUnblock(user);
            }
        });

        rvDrivers.setAdapter(driversAdapter);
        rvPassengers.setAdapter(passengersAdapter);

        btnRefresh.setOnClickListener(x -> reloadAll());

        reloadAll();
    }

    private void reloadAll() {
        showGlobalError(null);
        loadDrivers();
        loadPassengers();
    }

    private void loadDrivers() {
        if (loadingDrivers) return;
        loadingDrivers = true;

        setSectionStateLoading(true);

        repo.listUsersWithStatus("DRIVER", null, 500, new AdminUsersRepository.ListCb() {
            @Override
            public void onSuccess(List<AdminUserStatusResponseDto> list) {
                runOnUiThread(() -> {
                    loadingDrivers = false;
                    drivers.clear();
                    if (list != null) drivers.addAll(list);
                    driversAdapter.notifyDataSetChanged();
                    setSectionStateLoaded(true);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    loadingDrivers = false;
                    if (httpCode == 401) {
                        sessionExpired();
                        return;
                    }
                    setSectionStateError(true, msg, httpCode);
                });
            }
        });
    }

    private void loadPassengers() {
        if (loadingPassengers) return;
        loadingPassengers = true;

        setSectionStateLoading(false);

        repo.listUsersWithStatus("PASSENGER", null, 500, new AdminUsersRepository.ListCb() {
            @Override
            public void onSuccess(List<AdminUserStatusResponseDto> list) {
                runOnUiThread(() -> {
                    loadingPassengers = false;
                    passengers.clear();
                    if (list != null) passengers.addAll(list);
                    passengersAdapter.notifyDataSetChanged();
                    setSectionStateLoaded(false);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    loadingPassengers = false;
                    if (httpCode == 401) {
                        sessionExpired();
                        return;
                    }
                    setSectionStateError(false, msg, httpCode);
                });
            }
        });
    }

    private void setSectionStateLoading(boolean driversSection) {
        if (driversSection) {
            tvDriversState.setVisibility(View.VISIBLE);
            tvDriversState.setText("Loading drivers...");
            rvDrivers.setVisibility(View.GONE);
        } else {
            tvPassengersState.setVisibility(View.VISIBLE);
            tvPassengersState.setText("Loading passengers...");
            rvPassengers.setVisibility(View.GONE);
        }
    }

    private void setSectionStateLoaded(boolean driversSection) {
        if (driversSection) {
            if (drivers.isEmpty()) {
                tvDriversState.setVisibility(View.VISIBLE);
                tvDriversState.setText("No drivers found.");
                rvDrivers.setVisibility(View.GONE);
            } else {
                tvDriversState.setVisibility(View.GONE);
                rvDrivers.setVisibility(View.VISIBLE);
            }
        } else {
            if (passengers.isEmpty()) {
                tvPassengersState.setVisibility(View.VISIBLE);
                tvPassengersState.setText("No passengers found.");
                rvPassengers.setVisibility(View.GONE);
            } else {
                tvPassengersState.setVisibility(View.GONE);
                rvPassengers.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setSectionStateError(boolean driversSection, String msg, int httpCode) {
        String text = "Failed (HTTP " + httpCode + ")" + (msg != null && !msg.trim().isEmpty() ? ("\n" + msg) : "");
        if (driversSection) {
            tvDriversState.setVisibility(View.VISIBLE);
            tvDriversState.setText(text);
            rvDrivers.setVisibility(View.GONE);
        } else {
            tvPassengersState.setVisibility(View.VISIBLE);
            tvPassengersState.setText(text);
            rvPassengers.setVisibility(View.GONE);
        }
    }

    private void quickUnblock(AdminUserStatusResponseDto user) {
        if (user == null || user.getId() == null) return;

        showGlobalError(null);

        repo.setBlockStatus(user.getId(), false, null, new AdminUsersRepository.SaveCb() {
            @Override
            public void onSuccess(AdminUserStatusResponseDto updated) {
                runOnUiThread(() -> {
                    applyUpdate(updated);
                    Toast.makeText(AdminHomeActivity.this, "User unblocked.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 401) {
                        sessionExpired();
                        return;
                    }
                    showGlobalError("Unblock failed (HTTP " + httpCode + ")" + (msg != null ? ("\n" + msg) : ""));
                });
            }
        });
    }

    private void openBlockDialog(AdminUserStatusResponseDto user, boolean initialBlocked) {
        if (user == null || user.getId() == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_block_user, null, false);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvName = view.findViewById(R.id.tvDialogName);
        TextView tvEmail = view.findViewById(R.id.tvDialogEmail);

        RadioButton rbBlocked = view.findViewById(R.id.rbBlocked);
        RadioButton rbUnblocked = view.findViewById(R.id.rbUnblocked);
        EditText etReason = view.findViewById(R.id.etReason);

        TextView tvError = view.findViewById(R.id.tvDialogError);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        tvName.setText(safe(user.displayName()));
        tvEmail.setText(safe(user.getEmail()));

        boolean startBlocked = initialBlocked || user.isBlocked();

        rbBlocked.setChecked(startBlocked);
        rbUnblocked.setChecked(!startBlocked);

        etReason.setText(user.getBlockReason() != null ? user.getBlockReason() : "");
        etReason.setEnabled(startBlocked);
        etReason.setAlpha(startBlocked ? 1f : 0.6f);

        tvTitle.setText(startBlocked ? "Block user" : "Unblock user");

        rbBlocked.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                etReason.setEnabled(true);
                etReason.setAlpha(1f);
                tvTitle.setText("Block user");
            }
        });

        rbUnblocked.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                etReason.setEnabled(false);
                etReason.setAlpha(0.6f);
                tvTitle.setText("Unblock user");
            }
        });

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(x -> dlg.dismiss());

        btnSave.setOnClickListener(x -> {
            tvError.setVisibility(View.GONE);

            boolean blocked = rbBlocked.isChecked();
            String reason = etReason.getText() != null ? etReason.getText().toString() : null;

            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);

            repo.setBlockStatus(user.getId(), blocked, reason, new AdminUsersRepository.SaveCb() {
                @Override
                public void onSuccess(AdminUserStatusResponseDto updated) {
                    runOnUiThread(() -> {
                        applyUpdate(updated);
                        Toast.makeText(AdminHomeActivity.this,
                                blocked ? "User blocked." : "User unblocked.", Toast.LENGTH_SHORT).show();
                        dlg.dismiss();
                    });
                }

                @Override
                public void onError(String msg, int httpCode) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        btnCancel.setEnabled(true);

                        if (httpCode == 401) {
                            dlg.dismiss();
                            sessionExpired();
                            return;
                        }

                        String text = "Save failed (HTTP " + httpCode + ")"
                                + (msg != null && !msg.trim().isEmpty() ? ("\n" + msg) : "");
                        tvError.setText(text);
                        tvError.setVisibility(View.VISIBLE);
                    });
                }
            });
        });

        dlg.show();
    }

    private void applyUpdate(AdminUserStatusResponseDto updated) {
        if (updated == null || updated.getId() == null) return;

        boolean updatedDrivers = replaceInList(drivers, updated);
        boolean updatedPassengers = replaceInList(passengers, updated);

        if (updatedDrivers) {
            driversAdapter.notifyDataSetChanged();
            setSectionStateLoaded(true);
        }
        if (updatedPassengers) {
            passengersAdapter.notifyDataSetChanged();
            setSectionStateLoaded(false);
        }

        if (!updatedDrivers && !updatedPassengers) {
            reloadAll();
        }
    }

    private static boolean replaceInList(List<AdminUserStatusResponseDto> list, AdminUserStatusResponseDto updated) {
        if (list == null || updated == null || updated.getId() == null) return false;
        for (int i = 0; i < list.size(); i++) {
            AdminUserStatusResponseDto cur = list.get(i);
            if (cur != null && cur.getId() != null && cur.getId().longValue() == updated.getId().longValue()) {
                list.set(i, updated);
                return true;
            }
        }
        return false;
    }

    private void showGlobalError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvGlobalError.setVisibility(View.GONE);
        } else {
            tvGlobalError.setText(msg);
            tvGlobalError.setVisibility(View.VISIBLE);
        }
    }

    private void sessionExpired() {
        new TokenStorage(getApplicationContext()).clear();
        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
