package com.example.taximobile.feature.admin.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityChangePasswordBinding;

public class AdminChangePasswordActivity extends AdminBaseActivity {

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_change_password);
        binding = ActivityChangePasswordBinding.bind(v);

        toolbar.setTitle(getString(R.string.change_password_title));

        binding.btnConfirmPasswordChange.setOnClickListener(view -> {
            String oldPass = binding.etOldPassword.getText().toString().trim();
            String newPass = binding.etNewPassword.getText().toString().trim();
            String confirm = binding.etConfirmNewPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Password change requested (placeholder).", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
