package com.example.taximobile.feature.driver.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taximobile.R;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.databinding.ActivityDriverActivateAccountBinding;
import com.example.taximobile.feature.auth.ui.LoginActivity;
import com.example.taximobile.feature.driver.data.DriverActivationRepository;
import com.example.taximobile.feature.driver.data.dto.request.DriverActivateAccountRequestDto;

public class DriverActivateAccountActivity extends AppCompatActivity {

    private ActivityDriverActivateAccountBinding binding;
    private DriverActivationRepository repo;

    private String token;
    private boolean successShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverActivateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new TokenStorage(getApplicationContext()).clear();

        repo = new DriverActivationRepository(this);

        showError(null);
        showSuccess(null);
        setLoading(false);

        token = extractToken(getIntent());
        if (token == null || token.trim().isEmpty()) {
            showError(getString(R.string.driver_activate_error_missing_token));
            setFormEnabled(false);
        } else {
            setFormEnabled(true);
        }

        binding.btnActivate.setOnClickListener(v -> submit());
        binding.tvBackToLogin.setOnClickListener(v -> goToLogin());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        token = extractToken(intent);
        successShown = false;
        showSuccess(null);
        showError(null);

        if (token == null || token.trim().isEmpty()) {
            showError(getString(R.string.driver_activate_error_missing_token));
            setFormEnabled(false);
        } else {
            setFormEnabled(true);
        }
    }

    private void submit() {
        if (successShown) return;

        showError(null);
        showSuccess(null);

        if (token == null || token.trim().isEmpty()) {
            showError(getString(R.string.driver_activate_error_missing_token));
            return;
        }

        String pass = getText(binding.etPassword);
        String confirm = getText(binding.etConfirmPassword);

        if (pass.isEmpty() || confirm.isEmpty()) {
            showError(getString(R.string.driver_activate_error_fill_all));
            return;
        }

        if (pass.length() < 8) {
            showError(getString(R.string.driver_activate_error_min_len));
            return;
        }

        if (!pass.equals(confirm)) {
            showError(getString(R.string.driver_activate_error_mismatch));
            return;
        }

        setLoading(true);

        DriverActivateAccountRequestDto req =
                new DriverActivateAccountRequestDto(token.trim(), pass, confirm);

        repo.activate(req, new DriverActivationRepository.Cb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    successShown = true;
                    showSuccess(getString(R.string.driver_activate_success));

                    new Handler(Looper.getMainLooper()).postDelayed(
                            DriverActivateAccountActivity.this::goToLogin,
                            1500
                    );
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);

                    String text = (msg == null || msg.trim().isEmpty())
                            ? getString(R.string.driver_activate_failed)
                            : msg;

                    showError(text);
                });
            }
        });
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private String extractToken(Intent intent) {
        if (intent == null) return null;

        Uri data = intent.getData();
        if (data != null) {
            String t = data.getQueryParameter("token");
            if (t != null && !t.trim().isEmpty()) return t.trim();

            try {
                String last = data.getLastPathSegment();
                if (last != null && last.length() > 10) return last.trim();
            } catch (Exception ignored) {}
        }

        return null;
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnActivate.setEnabled(!loading && token != null && !token.trim().isEmpty() && !successShown);
        binding.etPassword.setEnabled(!loading && !successShown);
        binding.etConfirmPassword.setEnabled(!loading && !successShown);
    }

    private void setFormEnabled(boolean enabled) {
        binding.etPassword.setEnabled(enabled);
        binding.etConfirmPassword.setEnabled(enabled);
        binding.btnActivate.setEnabled(enabled && !successShown);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            binding.tvError.setText("");
            binding.tvError.setVisibility(View.GONE);
            return;
        }
        binding.tvError.setText(msg);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            binding.tvSuccess.setText("");
            binding.tvSuccess.setVisibility(View.GONE);
            return;
        }
        binding.tvSuccess.setText(msg);
        binding.tvSuccess.setVisibility(View.VISIBLE);
    }

    private String getText(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
