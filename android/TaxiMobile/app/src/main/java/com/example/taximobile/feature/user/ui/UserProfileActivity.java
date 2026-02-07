package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.auth.LogoutManager;
import com.example.taximobile.core.network.ApiConfig;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.databinding.ActivityUserProfileBinding;
import com.example.taximobile.feature.auth.ui.ChangePasswordActivity;
import com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto;
import com.example.taximobile.feature.user.data.UserProfileRepository;
import com.example.taximobile.feature.user.data.dto.request.UpdateUserProfileRequestDto;
import com.example.taximobile.feature.user.data.dto.response.UserProfileResponseDto;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends UserBaseActivity {

    private ActivityUserProfileBinding binding;
    private UserProfileRepository repo;

    private long userId;
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_profile);
        binding = ActivityUserProfileBinding.bind(v);

        toolbar.setTitle(getString(R.string.menu_profile));
        repo = new UserProfileRepository(this);

        String token = new TokenStorage(getApplicationContext()).getToken();
        Long id = token != null ? JwtUtils.getUserIdFromSub(token) : null;

        if (id == null || id <= 0) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            LogoutManager.logout(this);
            return;
        }
        userId = id;

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadImage(uri);
                }
        );

        binding.btnUploadPicture.setOnClickListener(view -> pickImageLauncher.launch("image/*"));
        binding.btnUpdateProfile.setOnClickListener(view -> updateProfile());
        binding.btnRequestPasswordChange.setOnClickListener(view ->
                startActivity(new Intent(this, ChangePasswordActivity.class))
        );

        loadProfile();
    }

    private void loadProfile() {
        setLoading(true);
        showError(null);
        showSuccess(null);

        repo.getProfile(userId, new UserProfileRepository.ProfileCb() {
            @Override
            public void onSuccess(UserProfileResponseDto dto) {
                runOnUiThread(() -> {
                    bindProfile(dto);
                    setLoading(false);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);

                    if (httpCode == 401) {
                        Toast.makeText(UserProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        LogoutManager.logout(UserProfileActivity.this);
                        return;
                    }

                    showError(getString(R.string.user_profile_load_failed) + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void bindProfile(UserProfileResponseDto dto) {
        if (dto == null) return;

        binding.etFirstName.setText(safe(dto.getFirstName()));
        binding.etLastName.setText(safe(dto.getLastName()));
        binding.etAddress.setText(safe(dto.getAddress()));
        binding.etPhone.setText(safe(dto.getPhoneNumber()));

        currentProfileImageUrl = safe(dto.getProfileImageUrl());
        loadAvatar(currentProfileImageUrl);
    }

    private void updateProfile() {
        setLoading(true);
        showError(null);
        showSuccess(null);

        UpdateUserProfileRequestDto req = new UpdateUserProfileRequestDto(
                trim(binding.etFirstName.getText() != null ? binding.etFirstName.getText().toString() : null),
                trim(binding.etLastName.getText() != null ? binding.etLastName.getText().toString() : null),
                trim(binding.etAddress.getText() != null ? binding.etAddress.getText().toString() : null),
                trim(binding.etPhone.getText() != null ? binding.etPhone.getText().toString() : null),
                normalizeStoredUrl(currentProfileImageUrl)
        );

        repo.updateProfile(userId, req, new UserProfileRepository.UpdateCb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    showSuccess(getString(R.string.user_profile_updated));
                    loadProfile();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);

                    if (httpCode == 401) {
                        Toast.makeText(UserProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        LogoutManager.logout(UserProfileActivity.this);
                        return;
                    }

                    showError(getString(R.string.user_profile_update_failed) + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void uploadImage(Uri uri) {
        setLoading(true);
        showError(null);
        showSuccess(null);

        repo.uploadProfileImage(userId, uri, new UserProfileRepository.UploadCb() {
            @Override
            public void onSuccess(ProfileImageUploadResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);

                    currentProfileImageUrl = dto != null ? safe(dto.getProfileImageUrl()) : "";
                    loadAvatar(currentProfileImageUrl);

                    showSuccess(getString(R.string.user_profile_image_uploaded));
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);

                    if (httpCode == 401) {
                        Toast.makeText(UserProfileActivity.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        LogoutManager.logout(UserProfileActivity.this);
                        return;
                    }

                    showError(getString(R.string.user_profile_image_upload_failed) + (msg != null ? (" (" + msg + ")") : ""));
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnUploadPicture.setEnabled(!loading);
        binding.btnUpdateProfile.setEnabled(!loading);
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

    private String safe(String s) { return s != null ? s : ""; }

    private String trim(String s) { return s == null ? "" : s.trim(); }
}
