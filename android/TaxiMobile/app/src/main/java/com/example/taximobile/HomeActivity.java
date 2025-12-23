package com.example.taximobile;

import android.os.Bundle;
import android.view.View;

import com.example.taximobile.databinding.ActivityHomeBinding;

public class HomeActivity extends DriverBaseActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_home);
        binding = ActivityHomeBinding.bind(contentView);

        toolbar.setTitle("Home");
    }
}
