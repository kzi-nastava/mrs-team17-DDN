package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.taximobile.R;

public class DriverHomeActivity extends DriverBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_home);
        toolbar.setTitle(getString(R.string.menu_home));

        TextView title = contentView.findViewById(R.id.textHome);
        TextView subtitle = contentView.findViewById(R.id.textHomeSubtitle);

        if (title != null) {
            title.setText(getString(R.string.driver_home_welcome));
        }
        if (subtitle != null) {
            subtitle.setText(getString(R.string.driver_home_subtitle));
        }
    }
}
