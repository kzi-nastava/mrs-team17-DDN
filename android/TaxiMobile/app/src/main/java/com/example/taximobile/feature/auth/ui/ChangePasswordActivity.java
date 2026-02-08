package com.example.taximobile.feature.auth.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.LogoutManager;
import com.example.taximobile.databinding.ActivityChangePasswordBinding;
import com.example.taximobile.feature.auth.data.AccountRepository;
import com.example.taximobile.feature.auth.data.dto.request.ChangePasswordRequestDto;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private AccountRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = new AccountRepository(this);

        setLoading(false);
        showError(null);
        showSuccess(null);

        binding.btnConfirmPasswordChange.setOnClickListener(v -> submit());
    }

    private void submit() {
        showError(null);
        showSuccess(null);

        String current = getText(binding.etOldPassword);
        String next = getText(binding.etNewPassword);
        String confirm = getText(binding.etConfirmNewPassword);

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            showError(getString(R.string.change_password_fill_all));
            return;
        }

        if (next.length() < 8) {
            showError(getString(R.string.change_password_min_len));
            return;
        }

        if (!next.equals(confirm)) {
            showError(getString(R.string.change_password_mismatch));
            return;
        }

        setLoading(true);

        ChangePasswordRequestDto req = new ChangePasswordRequestDto(current, next, confirm);
        repo.changePassword(req, new AccountRepository.Cb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ChangePasswordActivity.this,
                            getString(R.string.change_password_success),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);

                    if (httpCode == 401) {
                        Toast.makeText(ChangePasswordActivity.this,
                                getString(R.string.session_expired),
                                Toast.LENGTH_LONG).show();
                        LogoutManager.logout(ChangePasswordActivity.this);
                        return;
                    }

                    String text = (msg == null || msg.trim().isEmpty())
                            ? getString(R.string.change_password_failed)
                            : msg;

                    showError(text);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnConfirmPasswordChange.setEnabled(!loading);
        binding.etOldPassword.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmNewPassword.setEnabled(!loading);
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

    private String getText(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
