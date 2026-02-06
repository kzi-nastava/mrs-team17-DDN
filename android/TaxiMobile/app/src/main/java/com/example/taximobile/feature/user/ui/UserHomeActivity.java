package com.example.taximobile.feature.user.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.taximobile.R;

public class UserHomeActivity extends UserBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_home);
        toolbar.setTitle(getString(R.string.menu_home));

        TextView tv = contentView.findViewById(R.id.textHome);
        if (tv != null) {
            tv.setText(getString(R.string.user_home_welcome));
        }
    }
}
