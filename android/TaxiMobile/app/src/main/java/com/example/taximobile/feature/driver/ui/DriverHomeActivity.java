package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.View;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityHomeBinding;

public class DriverHomeActivity extends DriverBaseActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_home);
        binding = ActivityHomeBinding.bind(contentView);

        toolbar.setTitle("Home");
    }
}
