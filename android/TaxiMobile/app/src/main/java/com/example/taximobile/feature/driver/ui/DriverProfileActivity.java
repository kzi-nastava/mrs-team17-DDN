package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;


import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityDriverProfileBinding;
import com.example.taximobile.feature.auth.ui.ChangePasswordActivity;

public class DriverProfileActivity extends DriverBaseActivity {

    private ActivityDriverProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_profile);
        binding = ActivityDriverProfileBinding.bind(v);

        toolbar.setTitle("Profile");

        binding.etFirstName.setText("John");
        binding.etLastName.setText("Wall");
        binding.etAddress.setText("601 F Street NW, Washington, D.C.");
        binding.etPhone.setText("(828) 505-8760");
        binding.tvActiveTime.setText("Active time (last 24h): 3h 37min / 8h");

        binding.btnUploadPicture.setOnClickListener(view ->
                Toast.makeText(this, "Open Upload Picture screen (KT1 placeholder)", Toast.LENGTH_SHORT).show()
        );

        binding.btnRequestUpdate.setOnClickListener(view ->
                Toast.makeText(this, "Profile update request (KT1 placeholder)", Toast.LENGTH_SHORT).show()
        );

        binding.btnRequestPasswordChange.setOnClickListener(view ->
                startActivity(new Intent(this, ChangePasswordActivity.class))
        );

    }
}
