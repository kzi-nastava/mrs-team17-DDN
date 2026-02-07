package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.network.ApiConfig;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.databinding.ActivityDriverProfileBinding;
import com.example.taximobile.feature.driver.data.DriverProfileRepository;
import com.example.taximobile.feature.driver.data.dto.request.UpdateDriverProfileRequestDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.UserProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.VehicleInfoResponseDto;
import com.example.taximobile.feature.auth.ui.ChangePasswordActivity;
import com.example.taximobile.feature.auth.ui.LoginActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DriverProfileActivity extends DriverBaseActivity {

    private ActivityDriverProfileBinding binding;

    private DriverProfileRepository repo;

    private long driverId;
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_profile);
        binding = ActivityDriverProfileBinding.bind(v);

        toolbar.setTitle(getString(R.string.driver_profile_title));

        repo = new DriverProfileRepository(this);

        // Resolve driverId from JWT
        String token = new TokenStorage(getApplicationContext()).getToken();
        Long id = token != null ? JwtUtils.getDriverId(token) : null;
        if (id == null) {
            Toast.makeText(this, "Missing driverId in token. Please login again.", Toast.LENGTH_LONG).show();
            new TokenStorage(getApplicationContext()).clear();
            goToLogin();
            return;
        }
        driverId = id;

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadImage(uri);
                }
        );

        binding.btnUploadPicture.setOnClickListener(view -> pickImageLauncher.launch("image/*"));
        binding.btnRequestUpdate.setOnClickListener(view -> sendProfileChangeRequest());
        binding.btnRequestPasswordChange.setOnClickListener(view ->
                startActivity(new Intent(this, ChangePasswordActivity.class))
        );

        loadProfile();
    }

    private void loadProfile() {
        setLoading(true);
        showError(null);
        showSuccess(null);

        repo.getProfile(driverId, new DriverProfileRepository.ProfileCb() {
            @Override
            public void onSuccess(DriverProfileResponseDto dto) {
                runOnUiThread(() -> {
                    bindProfile(dto);
                    setLoading(false);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 401) {
                        new TokenStorage(getApplicationContext()).clear();
                        Toast.makeText(DriverProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLogin();
                        return;
                    }
                    setLoading(false);
                    showError("Cannot load profile data." + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void bindProfile(DriverProfileResponseDto dto) {
        UserProfileResponseDto driver = dto != null ? dto.getDriver() : null;
        VehicleInfoResponseDto vehicle = dto != null ? dto.getVehicle() : null;

        if (driver != null) {
            binding.etFirstName.setText(safe(driver.getFirstName()));
            binding.etLastName.setText(safe(driver.getLastName()));
            binding.etAddress.setText(safe(driver.getAddress()));
            binding.etPhone.setText(safe(driver.getPhoneNumber()));

            currentProfileImageUrl = safe(driver.getProfileImageUrl());
            loadAvatar(currentProfileImageUrl);

            // blocked notice
            boolean blocked = driver.isBlocked();
            binding.cardBlocked.setVisibility(blocked ? View.VISIBLE : View.GONE);
            if (blocked) {
                String reason = safe(driver.getBlockReason()).trim();
                if (reason.isEmpty()) {
                    binding.tvBlockedReason.setText(getString(R.string.blocked_no_reason));
                } else {
                    binding.tvBlockedReason.setText(getString(R.string.blocked_reason_prefix) + "\n" + reason);
                }
            }
        } else {
            binding.etFirstName.setText("");
            binding.etLastName.setText("");
            binding.etAddress.setText("");
            binding.etPhone.setText("");
            currentProfileImageUrl = "";
            binding.cardBlocked.setVisibility(View.GONE);
            binding.imgAvatar.setImageResource(R.drawable.avatar);
        }

        long active = dto != null ? dto.getActiveMinutesLast24h() : 0L;
        setActiveTime(active);

        if (vehicle != null) {
            binding.tvVehicleModel.setText(safe(vehicle.getModel()));
            binding.tvVehicleType.setText(formatVehicleType(vehicle.getType()));
            binding.tvVehiclePlate.setText(safe(vehicle.getLicensePlate()));
            binding.tvVehicleSeats.setText(String.valueOf(vehicle.getSeats()));
            binding.tvVehicleBaby.setText(vehicle.isBabyTransport() ? getString(R.string.yes) : getString(R.string.no));
            binding.tvVehiclePet.setText(vehicle.isPetTransport() ? getString(R.string.yes) : getString(R.string.no));
        } else {
            binding.tvVehicleModel.setText("");
            binding.tvVehicleType.setText("");
            binding.tvVehiclePlate.setText("");
            binding.tvVehicleSeats.setText("");
            binding.tvVehicleBaby.setText("");
            binding.tvVehiclePet.setText("");
        }
    }

    private void sendProfileChangeRequest() {
        setLoading(true);
        showError(null);
        showSuccess(null);

        UpdateDriverProfileRequestDto req = new UpdateDriverProfileRequestDto(
                trim(binding.etFirstName.getText() != null ? binding.etFirstName.getText().toString() : null),
                trim(binding.etLastName.getText() != null ? binding.etLastName.getText().toString() : null),
                trim(binding.etAddress.getText() != null ? binding.etAddress.getText().toString() : null),
                trim(binding.etPhone.getText() != null ? binding.etPhone.getText().toString() : null),
                normalizeStoredUrl(currentProfileImageUrl)
        );

        repo.requestProfileChange(driverId, req, new DriverProfileRepository.ChangeRequestCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSuccess(getString(R.string.profile_update_request_sent));
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 401) {
                        new TokenStorage(getApplicationContext()).clear();
                        Toast.makeText(DriverProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLogin();
                        return;
                    }
                    setLoading(false);
                    showError(getString(R.string.profile_update_request_failed) + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void uploadImage(Uri uri) {
        setLoading(true);
        showError(null);
        showSuccess(null);

        repo.uploadProfileImage(driverId, uri, new DriverProfileRepository.UploadCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);

                    currentProfileImageUrl = dto != null ? safe(dto.getProfileImageUrl()) : "";
                    loadAvatar(currentProfileImageUrl);

                    showSuccess(getString(R.string.profile_image_uploaded));
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    if (httpCode == 401) {
                        new TokenStorage(getApplicationContext()).clear();
                        Toast.makeText(DriverProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLogin();
                        return;
                    }
                    setLoading(false);
                    showError(getString(R.string.profile_image_upload_failed) + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void setActiveTime(long activeMinutes) {
        long safe = Math.max(0, activeMinutes);
        int max = 8 * 60;
        int shown = (int) Math.min(safe, max);

        String left = formatActiveMinutes(shown);
        binding.tvActiveTime.setText(getString(R.string.active_time_prefix) + " " + left + " / 8h");

        binding.progressActive.setMax(max);
        binding.progressActive.setProgress(shown);
    }

    private String formatActiveMinutes(int minutes) {
        int m = Math.max(0, minutes);
        int h = m / 60;
        int mm = m % 60;

        if (h <= 0) return mm + "min";
        if (mm <= 0) return h + "h";
        return h + "h " + mm + "min";
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnUploadPicture.setEnabled(!loading);
        binding.btnRequestUpdate.setEnabled(!loading);
        binding.btnRequestPasswordChange.setEnabled(!loading);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            binding.tvError.setVisibility(View.GONE);
            binding.tvError.setText("");
            return;
        }
        binding.tvError.setText(msg);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            binding.tvSuccess.setVisibility(View.GONE);
            binding.tvSuccess.setText("");
            return;
        }
        binding.tvSuccess.setText(msg);
        binding.tvSuccess.setVisibility(View.VISIBLE);
    }

    private void loadAvatar(String rawUrl) {
        String url = resolveImageUrl(rawUrl);
        if (url == null || url.trim().isEmpty()) {
            binding.imgAvatar.setImageResource(R.drawable.avatar);
            return;
        }

        new Thread(() -> {
            Bitmap bmp = null;
            HttpURLConnection conn = null;
            try {
                URL u = new URL(url);
                conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(8000);
                conn.setInstanceFollowRedirects(true);

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    try (InputStream in = conn.getInputStream()) {
                        bmp = BitmapFactory.decodeStream(in);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }

            Bitmap finalBmp = bmp;
            runOnUiThread(() -> {
                if (finalBmp != null) {
                    binding.imgAvatar.setImageBitmap(finalBmp);
                } else {
                    binding.imgAvatar.setImageResource(R.drawable.avatar);
                }
            });
        }).start();
    }

    private String resolveImageUrl(String url) {
        String u = safe(url).trim();
        if (u.isEmpty()) return "";

        if (u.startsWith("http://") || u.startsWith("https://")) return u;

        // backend returns " /public/... "
        if (u.startsWith("/public/")) {
            String origin = ApiConfig.BASE_URL;
            if (origin.endsWith("/")) origin = origin.substring(0, origin.length() - 1);
            return origin + u;
        }

        return u;
    }

    private String normalizeStoredUrl(String url) {
        String u = safe(url).trim();
        if (u.isEmpty()) return "";

        String origin = ApiConfig.BASE_URL;
        if (origin.endsWith("/")) origin = origin.substring(0, origin.length() - 1);

        if ((u.startsWith("http://") || u.startsWith("https://")) && u.startsWith(origin)) {
            return u.substring(origin.length());
        }

        return u;
    }

    private String formatVehicleType(String type) {
        String t = safe(type).trim().toLowerCase();
        if (t.isEmpty()) return "";
        if (t.equals("standard")) return "Standard";
        if (t.equals("luxury")) return "Luxury";
        if (t.equals("van") || t.equals("kombi")) return "Van";
        return type;
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private String trim(String s) {
        if (s == null) return "";
        return s.trim();
    }
}
