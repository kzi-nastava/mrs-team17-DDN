package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.LogoutManager;
import com.example.taximobile.feature.support.ui.SupportChatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class UserBaseActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navView;
    protected FrameLayout baseContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_base);

        toolbar = findViewById(R.id.baseToolbar);
        drawerLayout = findViewById(R.id.baseDrawer);
        navView = findViewById(R.id.baseNav);
        baseContent = findViewById(R.id.baseContent);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                if (!(this instanceof UserHomeActivity)) {
                    startActivity(new Intent(this, UserHomeActivity.class));
                }

            } else if (id == R.id.nav_support) {
                startActivity(new Intent(this, SupportChatActivity.class));

            } else if (id == R.id.nav_profile) {
                if (!(this instanceof UserProfileActivity)) {
                    startActivity(new Intent(this, UserProfileActivity.class));
                }

            } else if (id == R.id.nav_logout) {
                LogoutManager.logout(this);
                drawerLayout.closeDrawers();
                return true;

            } else {
                Intent i = new Intent(this, UserPlaceholderActivity.class);
                i.putExtra(UserPlaceholderActivity.EXTRA_TITLE, String.valueOf(item.getTitle()));
                startActivity(i);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    protected View inflateContent(int layoutResId) {
        View v = getLayoutInflater().inflate(layoutResId, baseContent, false);
        baseContent.addView(v);
        return v;
    }
}
