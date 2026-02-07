package com.example.taximobile.feature.admin.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.taximobile.R;

public class AdminHomeActivity extends AdminBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_home);
        toolbar.setTitle(getString(R.string.menu_home));

        TextView tv = contentView.findViewById(R.id.textHome);
        TextView subtitle = contentView.findViewById(R.id.textHomeSubtitle);
        if (tv != null) {
            tv.setText(getString(R.string.admin_home_welcome));
        }
        if (subtitle != null) {
            subtitle.setText(getString(R.string.admin_home_subtitle));
        }
    }
}
