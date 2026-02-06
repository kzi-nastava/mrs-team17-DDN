package com.example.taximobile.feature.user.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.taximobile.R;

public class UserPlaceholderActivity extends UserBaseActivity {

    public static final String EXTRA_TITLE = "extra_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = inflateContent(R.layout.activity_placeholder);

        String title = getIntent() != null ? getIntent().getStringExtra(EXTRA_TITLE) : null;
        if (title == null || title.trim().isEmpty()) {
            title = getString(R.string.placeholder_title);
        }

        toolbar.setTitle(title);

        TextView tvTitle = contentView.findViewById(R.id.tvPlaceholderTitle);
        TextView tvBody = contentView.findViewById(R.id.tvPlaceholderBody);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvBody != null) tvBody.setText(getString(R.string.placeholder_not_implemented));
    }
}
