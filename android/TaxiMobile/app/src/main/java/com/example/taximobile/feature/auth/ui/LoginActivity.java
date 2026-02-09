package com.example.taximobile.feature.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.admin.ui.AdminHomeActivity;
import com.example.taximobile.feature.auth.data.AuthRepository;
import com.example.taximobile.feature.driver.ui.DriverHomeActivity;
import com.example.taximobile.feature.user.ui.PassengerActiveRideActivity;
import com.example.taximobile.feature.user.ui.UserHomeActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_POST_LOGIN_RIDE_ID = "extra_post_login_ride_id";

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private Button btnLogin;

    private AuthRepository authRepo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepo = new AuthRepository(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> onLoginClicked());

        // If token exists, skip login and route based on role
        tryAutoRoute();
    }

    private void tryAutoRoute() {
        String token = new TokenStorage(getApplicationContext()).getToken();
        if (token == null || token.isBlank()) return;

        routeByRole(token);
    }

    private void onLoginClicked() {
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!isValidEmail(email)) {
            showError(getString(R.string.login_error_invalid_email));
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 4) {
            showError(getString(R.string.login_error_invalid_password));
            return;
        }

        setLoading(true);

        authRepo.login(email, password, new AuthRepository.LoginCb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);

                    String token = new TokenStorage(getApplicationContext()).getToken();
                    if (token == null || token.isBlank()) {
                        showError(getString(R.string.login_error_missing_token));
                        return;
                    }

                    routeByRole(token);
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

    private void routeByRole(String token) {
        String role = JwtUtils.getRole(token);

        if (role == null || role.isBlank()) {
            new TokenStorage(getApplicationContext()).clear();
            showError(getString(R.string.login_error_invalid_token));
            return;
        }

        if ("DRIVER".equalsIgnoreCase(role)) {
            startActivity(new Intent(LoginActivity.this, DriverHomeActivity.class));
            finish();
            return;
        }

        if ("PASSENGER".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role)) {
            long deepLinkRideId = getIntent().getLongExtra(EXTRA_POST_LOGIN_RIDE_ID, -1L);
            if (deepLinkRideId > 0) {
                Intent i = new Intent(LoginActivity.this, PassengerActiveRideActivity.class);
                i.putExtra(PassengerActiveRideActivity.EXTRA_RIDE_ID, deepLinkRideId);
                startActivity(i);
            } else {
                startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
            }
            finish();
            return;
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
            finish();
            return;
        }

        new TokenStorage(getApplicationContext()).clear();
        showError(getString(R.string.login_error_role_not_supported));
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.loading_dots) : getString(R.string.btn_login));
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
