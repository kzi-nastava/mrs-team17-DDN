package com.example.taximobile.feature.driver.ui;

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

public abstract class DriverBaseActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navView;
    protected FrameLayout baseContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver_base);

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
                startActivity(new Intent(this, DriverHomeActivity.class));

            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, DriverRideHistoryActivity.class));

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, DriverProfileActivity.class));

            } else if (id == R.id.nav_support) {
                // ISTI support chat kao passenger
                startActivity(new Intent(this, SupportChatActivity.class));

            } else if (id == R.id.nav_active_ride) {
                startActivity(new Intent(this, DriverActiveRideActivity.class));

            }else if (id == R.id.nav_logout) {
                LogoutManager.logout(this);
                drawerLayout.closeDrawers();
                return true;
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
